#include "decoder.h"

#define finalize(codec_ctx, ctx, swr)                                                                                  \
	swr_free(swr);                                                                                                     \
	avcodec_close(*codec_ctx);                                                                                         \
	avcodec_free_context(codec_ctx);                                                                                   \
	avformat_close_input(ctx);

#define expand_and_handle_arr(arr, dst_size)                                                                     \
	if (ensure_capacity(&arr, dst_size) != SUCCESS)                                                             \
	{                                                                                                                  \
		MEM_HANDLING;                                                                                                  \
	}

int handle_averror(int val)
{
	switch (val)
	{
	case AVERROR_BSF_NOT_FOUND:
	case AVERROR_DECODER_NOT_FOUND:
	case AVERROR_ENCODER_NOT_FOUND:
	case AVERROR_FILTER_NOT_FOUND:
	case AVERROR_MUXER_NOT_FOUND:
	case AVERROR_OPTION_NOT_FOUND:
	case AVERROR_STREAM_NOT_FOUND:
	case AVERROR(ENOENT):
	case AVERROR(EINVAL):
	case AVERROR_BUG:
	case AVERROR_BUG2:
	case AVERROR_INVALIDDATA:
	case AVERROR_EXTERNAL:
		fprintf(stderr, "FILE IS INCORRECT\n");
		return ERROR_DATA_INVALID;
	case AVERROR_UNKNOWN:
		fprintf(stderr, "UNKNOWN ERROR\n");
		return ERROR_UNKNOWN;
	case AVERROR(ENOMEM):
		fprintf(stderr, "NOT ENOUGH MEMORY\n");
		return ERROR_NOTENOUGH_MEMORY;
	default:
		if (val < 0)
		{
			fprintf(stderr, "FILE IS INCORRECT\n");
			return ERROR_FORMAT_INVALID;
		}
		return SUCCESS;
	}
}

int open_format_ctx(char *file, AVFormatContext **out)
{
	int err = SUCCESS;

	if (((err = handle_averror(avformat_open_input(out, file, NULL, NULL))) != SUCCESS) ||
		(err = handle_averror(avformat_find_stream_info(*out, NULL))) != SUCCESS)
	{
		return err;
	}

	return err;
}

int find_audio_stream(AVFormatContext **ctx, int nb, AVStream **out)
{
	int count = 0;
	for (unsigned int i = 0; i < (*ctx)->nb_streams; i++)
	{
		if ((*ctx)->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			if (count == nb - 1)
			{
				*out = (*ctx)->streams[i];
				return SUCCESS;
			}
			count++;
		}
	}
	fprintf(stderr, "THERE ARE NO AUDIO STREAMS IN THE FILE\n");
	return ERROR_FORMAT_INVALID;
}

int open_decoder(AVStream **stream, const AVCodec **codec, AVCodecContext **codec_ctx)
{
	int err = SUCCESS;
	// define codec
	*codec = avcodec_find_decoder((*stream)->codecpar->codec_id);

	if (*codec == NULL)
	{
		fprintf(stderr, "FAILED TO FIND DECODER\n");
		return ERROR_FORMAT_INVALID;
	}

	// allocating memory for decoder;
	*codec_ctx = avcodec_alloc_context3(*codec);

	if (*codec_ctx == NULL)
	{
		fprintf(stderr, "FAILED TO ALLOCATE CODE CONTEXT\n");
		return ERROR_NOTENOUGH_MEMORY;
	}

	// stream parameters to codec context
	if ((err = handle_averror(avcodec_parameters_to_context(*codec_ctx, (*stream)->codecpar))) != SUCCESS)
	{
		return err;
	}

	// decoder initialization
	// not handle_averror because not AVERROR
	if (avcodec_open2(*codec_ctx, *codec, NULL) < 0)
	{
		fprintf(stderr, "FAILED TO INITIALIZE DECODER\n");
		return ERROR_FORMAT_INVALID;
	}
	return SUCCESS;
}

int swr_open(SwrContext **swr, AVCodecContext **codec_ctx, int sample_rate, int channel_mode)
{
	int err = SUCCESS;

	if (channel_mode != 1 && channel_mode != 2)
	{
		fprintf(stderr, "WRONG CHANNEL MODE\n");
		err = ERROR_FORMAT_INVALID;
		goto cleanup;
	}

	*swr = swr_alloc();
	if (swr == NULL)
	{
		MEM_HANDLING;
	}

	av_opt_set_int(*swr, "in_channel_layout", (int64_t)(*codec_ctx)->channel_layout, 0);
	if (channel_mode == 2)
	{
		av_opt_set_int(*swr, "out_channel_layout", AV_CH_LAYOUT_STEREO, 0);
	}
	else
	{
		av_opt_set_int(*swr, "out_channel_layout", AV_CH_LAYOUT_MONO, 0);
	}
	av_opt_set_int(*swr, "in_sample_rate", (*codec_ctx)->sample_rate, 0);
	av_opt_set_int(*swr, "out_sample_rate", sample_rate, 0);
	av_opt_set_int(*swr, "in_sample_fmt", (*codec_ctx)->sample_fmt, 0);
	av_opt_set_int(*swr, "out_sample_fmt", AV_SAMPLE_FMT_DBLP, 0);

	err = handle_averror(swr_init(*swr));

cleanup:
	return err;
}

int getSamples(AVFormatContext **ctx, AVCodecContext **codec_ctx, SwrContext **swr, audio *out, audio *out2, int chanel)
{
	// decoding
	int err = SUCCESS;
	int count = 0;
	AVPacket *pkt = av_packet_alloc();
	AVFrame *frame = av_frame_alloc();

	double **buf = NULL;
	buf = malloc(chanel * sizeof(double *));
	if (buf == NULL)
	{
		MEM_HANDLING;
	}

	buf[0] = out->wav_data.val;

	if (chanel == 2)
	{
		buf[1] = out2->wav_data.val;
	}

	if (pkt == NULL || frame == NULL)
	{
		MEM_HANDLING;
	}

	while ((err = av_read_frame(*ctx, pkt)) != AVERROR_EOF)
	{
		if (err < 0)
		{
			fprintf(stderr, "FAILED TO READ FRAME\n");
			err = ERROR_DATA_INVALID;
			goto cleanup;
		}

		err = avcodec_send_packet(*codec_ctx, pkt);
		if (err == AVERROR_EOF)
		{
			break;
		}

		if ((err = handle_averror(err)) != SUCCESS)
		{
			goto cleanup;
		}

		while ((err = avcodec_receive_frame(*codec_ctx, frame)) == SUCCESS)
		{
			if (chanel == 2 && frame->channels != 2 || frame->channels < 1)
			{
				fprintf(stderr, "WRONG NUMBER OF CHANELS\n");
				err = ERROR_FORMAT_INVALID;
				goto cleanup;
			}

			expand_and_handle_arr(out->wav_data, count + frame->nb_samples);
			buf[0] = &out->wav_data.val[count];

			if (out2 != NULL)
			{
				expand_and_handle_arr(out2->wav_data, count + frame->nb_samples);
				buf[1] = &out2->wav_data.val[count];
			}

			// flush swr
			// it can store some not necessary data from not used channels if channel_mode is 1
			//  it happens because swr bufferize data that didn't fit into buf because buf has not enough subarrays
			// so that's why it is necessary to flush swr before converting new frame
			int nb = swr_convert(*swr, NULL, frame->nb_samples, NULL, 0);
			// conversion
			nb = swr_convert(*swr, (uint8_t **)buf, frame->nb_samples, (const uint8_t **)&frame->data, frame->nb_samples);

			if (nb < 0)
			{
				fprintf(stderr, "FAILED TO CONVERT FRAME\n");
				err = ERROR_DATA_INVALID;
				goto cleanup;
			}

			count += nb;
			av_frame_unref(frame);
			av_packet_unref(pkt);
		}

		if (err != AVERROR_EOF && err != AVERROR(EAGAIN) && err != SUCCESS)
		{
			err = handle_averror(err);
			goto cleanup;
		}
	}
	out->sample_amount = count;
	if (out2 != NULL)
	{
		out2->sample_amount = count;
	}

	// to avoid influence of AVERROR_EOF and positive avcodec "NOT ERRORS"
	err = SUCCESS;

cleanup:
	free(buf);
	av_frame_free(&frame);
	av_packet_free(&pkt);
	return err;
}

int pre_decode(char *file, AVFormatContext **ctx, AVStream **stream, AVCodec **codec, AVCodecContext **codec_ctx, audio **res)
{
	int err = SUCCESS;
	if (((err = open_format_ctx(file, ctx)) != SUCCESS) || ((err = find_audio_stream(ctx, 1, stream)) != SUCCESS) ||
		((err = open_decoder(stream, codec, codec_ctx)) != SUCCESS))
	{
		goto out;
	};

	(*res)->sample_rate = (*stream)->codecpar->sample_rate;
out:
	return err;
}

int decode2(char *file1, char *file2, audio *decoded1, audio *decoded2)
{
	int err = SUCCESS;
	AVFormatContext *ps_f1 = NULL;
	AVStream *stream_f1 = NULL;
	AVCodec *codec_f1 = NULL;
	AVCodecContext *codec_ctx_f1 = NULL;

	AVFormatContext *ps_f2 = NULL;
	AVStream *stream_f2 = NULL;
	AVCodec *codec_f2 = NULL;
	AVCodecContext *codec_ctx_f2 = NULL;

	struct SwrContext *swr_f1 = NULL;
	struct SwrContext *swr_f2 = NULL;

	if (((err = pre_decode(file1, &ps_f1, &stream_f1, &codec_f1, &codec_ctx_f1, &decoded1)) != SUCCESS) ||
		((err = pre_decode(file2, &ps_f2, &stream_f2, &codec_f2, &codec_ctx_f2, &decoded2)) != SUCCESS))
	{
		goto cleanup;
	}

	int sample_rate = max(codec_ctx_f1->sample_rate, codec_ctx_f2->sample_rate);

	if (((err = swr_open(&swr_f1, &codec_ctx_f1, sample_rate, 1)) != SUCCESS) ||
		((err = swr_open(&swr_f2, &codec_ctx_f2, sample_rate, 1)) != SUCCESS) ||
		((err = getSamples(&ps_f1, &codec_ctx_f1, &swr_f1, decoded1, NULL, 1)) != SUCCESS) ||
		((err = getSamples(&ps_f2, &codec_ctx_f2, &swr_f2, decoded2, NULL, 1)) != SUCCESS))
	{
		goto cleanup;
	}

	decoded1->sample_rate = sample_rate;
	decoded2->sample_rate = sample_rate;

cleanup:
	finalize(&codec_ctx_f1, &ps_f1, &swr_f1);
	finalize(&codec_ctx_f2, &ps_f2, &swr_f2);
	return err;
}

int decode1(char *file, audio *decoded1, audio *decoded2)
{
	int err = SUCCESS;
	AVFormatContext *ps = NULL;
	AVStream *stream = NULL;
	AVCodec *codec = NULL;
	AVCodecContext *codec_ctx = NULL;

	struct SwrContext *swr = NULL;

	if (((err = pre_decode(file, &ps, &stream, &codec, &codec_ctx, &decoded1)) != SUCCESS) ||
		((err = swr_open(&swr, &codec_ctx, codec_ctx->sample_rate, 2)) != SUCCESS) ||
		((err = getSamples(&ps, &codec_ctx, &swr, decoded1, decoded2, 2)) != SUCCESS))
	{
		goto cleanup;
	}

cleanup:
	finalize(&codec_ctx, &ps, &swr);
	return err;
}
