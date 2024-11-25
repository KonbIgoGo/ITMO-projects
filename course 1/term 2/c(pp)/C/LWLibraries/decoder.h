
#ifndef DECODER
#define DECODER
#include "audio_fmt.h"
#include "return_codes.h"
#include "dynamic_arr.h"
#include "util.h"
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>

int decode1(char *file, audio *decoded1, audio *decoded2);
int decode2(char *file1, char *file2, audio *decoded1, audio *decoded2);

#endif
