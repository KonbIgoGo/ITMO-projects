#include "audio_fmt.h"
#include "decoder.h"
#include "util.h"
#include "xcorr.h"

#include <stdio.h>

#define EXT_LIST "opus,flac,mp2,mp3,aac"

#define CHECK_ERR                                                                                                      \
	if (err != SUCCESS)                                                                                                \
	{                                                                                                                  \
		goto cleanup;                                                                                                  \
	}

int main(int argc, char *argv[])
{
	// warning suppress
	av_log_set_level(0);

	char *file1 = argv[1];
	char *file2 = argv[2];
	uint32_t err = SUCCESS;

	if (argc > 3 || argc < 2)
	{
		fprintf(stderr, "INCORRECT ARGUMENT AMOUNT");
		return ERROR_ARGUMENTS_INVALID;
	}

	audio f1;
	audio f2;
	init_arr(&f1.wav_data);
	init_arr(&f2.wav_data);
	if (f1.wav_data.val == NULL || f2.wav_data.val == NULL)
	{
		MEM_HANDLING;
	}

	if (!av_match_ext(file1, EXT_LIST))
	{
		fprintf(stderr, "INCORRECT EXTENSION OF AUDIO FILE: ");
		fprintf(stderr, "%s\n", file1);
		err = ERROR_FORMAT_INVALID;
		goto cleanup;
	}

	if (argc == 3)
	{
		if (!av_match_ext(file2, EXT_LIST))
		{
			fprintf(stderr, "INCORRECT EXTENSION OF AUDIO FILE: ");
			fprintf(stderr, "%s\n", file2);
			err = ERROR_FORMAT_INVALID;
			goto cleanup;
		}
		err = decode2(file1, file2, &f1, &f2);
	}
	else
	{
		err = decode1(file1, &f1, &f2);
	}

	CHECK_ERR

	printf("%i, %i\n", f1.wav_data.size, f2.wav_data.size);

	delta r;
	x_corr(&f1, &f2, &r);

	printf("delta: %i samples\nsample rate: %i Hz\ndelta time: %i ms\n", r.d_samples, r.rate, r.d_time);

cleanup:
	free(f1.wav_data.val);
	free(f2.wav_data.val);
	return err;
}
