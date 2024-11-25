#include "dynamic_arr.h"

int init_arr(dynamic_arr *arr)
{
	arr->val = malloc(10 * sizeof(double));
	if (arr->val == NULL)
	{
		return ERROR_NOTENOUGH_MEMORY;
	}
	return SUCCESS;
}

int ensure_capacity(dynamic_arr *arr, int req_size)
{
	if (arr->size < req_size)
	{
		arr->size = req_size * 2;
		double *tmp = (double *)realloc(arr->val, (arr->size) * sizeof(double));
		if (tmp != NULL)
		{
			arr->val = tmp;
			return SUCCESS;
		}
		return ERROR_NOTENOUGH_MEMORY;
	}
	return SUCCESS;
}
