#ifndef AUDIOFMT
#define AUDIOFMT
#include <stdint.h>
#include "dynamic_arr.h"

typedef struct decoded_audio
{
	dynamic_arr wav_data;
	uint32_t sample_rate;
	int sample_amount;
} audio;

typedef struct delta
{
	int rate;
	int d_time;
	int d_samples;
} delta;

#endif	  // !AUDIOFMT
