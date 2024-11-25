#ifndef DYNARR
#define DYNARR
#include "return_codes.h"

#include <stdint.h>
#include <stdlib.h>

typedef struct dynamic_arr
{
	double *val;
	int size;
} dynamic_arr;

int init_arr(dynamic_arr *arr);
int ensure_capacity(dynamic_arr *arr, int req_size);

#endif	  // !DYNARR
