#include "return_codes.h"

#include <math.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

#define HMANT32 8388608
#define HMANT16 1024
#define MANT16 10
#define MANT32 23
#define BIAS32 127
#define BIAS16 15
#define MAX_EXP32 255
#define MAX_EXP16 31

typedef struct float_num
{
	bool sign;
	int16_t exp;
	uint32_t mant;
} float_num;

float_num zero(bool sign, char mode)
{
	float_num res;
	res.sign = sign;
	res.mant = 0;
	res.exp = mode == 'h' ? BIAS16 : BIAS32;
	return res;
}

float_num nan_t(char mode)
{
	float_num res;
	res.sign = false;
	res.exp = mode == 'h' ? MAX_EXP16 : MAX_EXP32;
	res.mant = 1;
	return res;
}

float_num inf(bool sign, char mode)
{
	float_num res;
	res.sign = sign;
	res.exp = mode == 'h' ? MAX_EXP16 : MAX_EXP32;
	res.mant = 0;
	return res;
}

float_num max_num(char mode)
{
	float_num res;
	res.sign = false;
	if (mode == 'h')
	{
		res.exp = MAX_EXP16;
		res.mant = HMANT16 >> 1;
	}
	else
	{
		res.exp = MAX_EXP32;
		res.mant = HMANT32 >> 1;
	}
	return res;
}

bool is_zero(float_num num, char mode)
{
	if (((num.exp == BIAS32 && mode == 'f') || (num.exp == BIAS16 && mode == 'h')) && num.mant == 0)
	{
		return 1;
	}
	return 0;
}

bool is_nan(float_num num, char mode)
{
	if (((num.exp == MAX_EXP32 && mode == 'f') || (num.exp == MAX_EXP16 && mode == 'h')) && num.mant != 0)
	{
		return 1;
	}
	return 0;
}

bool is_inf(float_num num, char mode)
{
	if (((num.exp == MAX_EXP32 && mode == 'f') || (num.exp == MAX_EXP16 && mode == 'h')) && num.mant == 0)
	{
		return 1;
	}
	return 0;
}

bool out_special(float_num num, char mode, uint8_t round)
{
	if ((num.exp > MAX_EXP32 && mode == 'f') || (num.exp > MAX_EXP16 && mode == 'h'))
	{
		switch (round)
		{
		case 0:
			num = max_num(mode);
			break;
		case 1:
			num = inf(num.sign, mode);
			break;
		case 2:
			num = inf(false, mode);
			break;
		case 3:
			num = inf(true, mode);
			break;
		default:
			break;
		}
		num = max_num(mode);
	}

	if (is_zero(num, mode))
	{
		if (num.sign == 1)
		{
			printf("-");
		}
		if (mode == 'f')
		{
			printf("0x0.%06xp%+i\n", num.mant * 2, num.exp - BIAS32);
		}
		else
		{
			printf("0x0.%03xp%+i\n", num.mant * 2, num.exp - BIAS16);
		}
		return 1;
	}
	else if (is_nan(num, mode))
	{
		printf("nan\n");
		return 1;
	}
	else if (is_inf(num, mode))
	{
		if (num.sign == 1)
		{
			printf("-");
		}
		printf("inf\n");
		return 1;
	}
	return 0;
}

void out(float_num num, char mode, uint8_t round)
{
	if (out_special(num, mode, round) == 0)
	{
		if (num.sign == true)
		{
			printf("-");
		}
		if (mode == 'f')
		{
			printf("0x1.%06xp%+i\n", num.mant * 2, num.exp - BIAS32);
		}
		else if (mode == 'h')
		{
			printf("0x1.%03xp%+i\n", ((num.mant) * 2), num.exp - BIAS16);
		}
	};
}

uint8_t define_len(uint64_t num)
{
	uint8_t counter = 0;
	while (num != 0)
	{
		num >>= 1;
		counter++;
	}
	return counter;
}

uint32_t power_of_two(uint8_t pow)
{
	return ((uint32_t)1 << pow);
}

uint8_t round_check(uint32_t rest, uint32_t mant, uint8_t sign, uint8_t rest_len, uint8_t round)
{
	if (round == 1 && (rest >= power_of_two(rest_len - 1) || ((mant) % 2 == 0)))
	{
		return 1;
	}
	else if (round == 2 && (rest > 0 && sign == 0))
	{
		return 1;
	}
	else if (round == 3 && (rest > 0 && sign == 1))
	{
		return 1;
	}

	return 0;
}

float_num converter(int32_t num, char mode)
{
	float_num converted;
	uint8_t mant_shift;
	uint8_t exp_shift;
	if (mode == 'f')
	{
		converted.mant = ((unsigned)num << 9) >> 9;
		converted.exp = ((unsigned)num << 1) >> 24;
		converted.sign = (unsigned)num >> MAX_EXP16;
		mant_shift = 24;
		exp_shift = MANT32;
	}
	else
	{
		converted.exp = ((unsigned)num << (1 + 16)) >> (11 + 16);
		converted.mant = ((unsigned)num << (6 + 16)) >> (6 + 16);
		converted.sign = (unsigned)num >> BIAS16;
		mant_shift = 11;
		exp_shift = MANT16;
	}

	if (converted.exp == 0)
	{
		uint8_t len = define_len(converted.mant);
		converted.mant <<= (mant_shift - len);
		converted.mant -= mode == 'f' ? HMANT32 : HMANT16;
		converted.exp -= (exp_shift - len);
	}

	if (num == 0 || (num == power_of_two(BIAS16) && mode == 'h') || (num == power_of_two(MAX_EXP16) && mode == 'f'))
	{
		return zero(converted.sign, mode);
	}

	return converted;
}

float_num multiplicator(float_num num1, float_num num2, uint8_t round, char mode)
{
	float_num res;

	bool nan1 = is_nan(num1, mode);
	bool nan2 = is_nan(num2, mode);
	bool inf1 = is_inf(num1, mode);
	bool inf2 = is_inf(num2, mode);
	bool zero1 = is_zero(num1, mode);
	bool zero2 = is_zero(num2, mode);

	res.sign = num1.sign ^ num2.sign;
	if (nan1 || nan2 || (inf1 && zero2) || (inf2 && zero1))
	{
		return nan_t(mode);
	}
	else if (zero1 || zero2)
	{
		return zero(res.sign, mode);
	}
	else if (inf1 || inf2)
	{
		return inf(res.sign, mode);
	}

	uint8_t exp_bias;
	uint32_t hmant;
	uint8_t mant_len;
	if (mode == 'h')
	{
		exp_bias = BIAS16;
		hmant = HMANT16;
		mant_len = MANT16;
	}
	else
	{
		exp_bias = BIAS32;
		hmant = HMANT32;
		mant_len = MANT32;
	}

	res.exp = num1.exp - exp_bias + num2.exp;

	uint64_t mant = ((uint64_t)num1.mant + hmant) * ((uint64_t)num2.mant + hmant);

	uint8_t len = define_len(mant);

	uint64_t temp = mant;

	uint32_t rest = (uint32_t)(mant << (65 - len + mant_len) >> (65 - len + mant_len));

	mant = (mant << (65 - len)) >> (65 - len);
	if (mant != temp)
	{
		len -= 1;
		res.exp += 1;
	}

	uint8_t rest_len = len - mant_len;
	res.mant = (uint32_t)(mant >> (rest_len));
	res.mant += round_check(rest, res.mant, res.sign, rest_len, round);

	res.mant = mode == 'h' ? res.mant << 1 : res.mant;
	// idk why it is necessary o_0 but it is (0x4145 * 0x42eb mant without rounding: 00100011100 && mant with rounding:
	// 00100011110 (+2? && len 11 ???) )
	return res;
}

float_num divisor(float_num num1, float_num num2, uint8_t round, char mode)
{
	float_num res;

	bool nan1 = is_nan(num1, mode);
	bool nan2 = is_nan(num2, mode);
	bool inf1 = is_inf(num1, mode);
	bool inf2 = is_inf(num2, mode);
	bool zero1 = is_zero(num1, mode);
	bool zero2 = is_zero(num2, mode);

	res.sign = num1.sign ^ num2.sign;

	if (nan1 || nan2 || (inf1 && inf2))
	{
		return nan_t(mode);
	}
	else if (zero1)
	{
		return zero(res.sign, mode);
	}
	else if (inf1 || inf2 || zero2)
	{
		return inf(res.sign, mode);
	}

	uint32_t mant_size;
	uint32_t hmant;
	uint8_t exp_bias;

	if (mode == 'f')
	{
		mant_size = MANT32;
		hmant = HMANT32;
		exp_bias = BIAS32;
	}
	else
	{
		mant_size = MANT16;
		hmant = HMANT16;
		exp_bias = BIAS16;
	}

	res.exp = num1.exp - (num2.exp - exp_bias);

	uint64_t mant = (((uint64_t)num1.mant + hmant) << (mant_size + 2)) / ((uint64_t)num2.mant + hmant);
	// MANT32 + 2 shift for rounding (1 + 22(9) hmant + mant + 1 part of mant + 1 the rest)
	uint8_t len = define_len(mant);
	uint64_t temp = mant;
	mant = (mant << (65 - len)) >> (65 - len);
	if (mant != temp)
	{
		if (len < mant_size + 3)	// else : hmant already normalized (hmant on 24th(11th) bit of mant + 2 bit rest)
		{
			res.exp--;
		}
		len--;
	}

	if (len > mant_size)
	{
		uint32_t rest = (uint32_t)(mant << (65 - (len - mant_size))) >> (65 - (len - mant_size));
		res.mant = mant >> (len - mant_size);
		res.mant += round_check(rest, res.mant, res.sign, len - mant_size, round);
	}
	return res;
}

uint32_t mant_sum_counter(float_num h, float_num l, uint8_t round, char mode)
{
	uint8_t diff = h.exp - l.exp;

	uint8_t shift;
	if (mode == 'f')
	{
		shift = MANT32;
		l.mant += HMANT32;
	}
	else
	{
		shift = MANT16;
		l.mant += HMANT16;
	}

	l.mant <<= 7;
	h.mant <<= 8;
	for (int i = 0; i < diff - 1; i++)
	{
		l.mant >>= 1;
	}
	uint32_t rest;
	uint32_t mant = l.mant + h.mant;
	uint8_t rest_len = 8;

	if ((mant << 1) >> 1 != mant)
	{
		rest = (mant << shift) >> shift;
		mant <<= 1;
		mant >>= 1;
		rest_len += 1;
	}
	else
	{
		rest = (mant << (shift + 1)) >> (shift + 1);
	}

	return (mant >> rest_len) + round_check(rest, mant >> rest_len, h.sign, rest_len, round);
}

float_num summator(float_num num1, float_num num2, uint8_t round, char mode)
{
	bool nan1 = is_nan(num1, mode);
	bool nan2 = is_nan(num2, mode);
	bool inf1 = is_inf(num1, mode);
	bool inf2 = is_inf(num2, mode);
	bool zero1 = is_zero(num1, mode);
	bool zero2 = is_zero(num2, mode);

	float_num res;

	if (inf1 && inf2 || nan1 || nan2)
	{
		return nan_t(mode);
	}
	else if (zero1 || inf2)
	{
		return num2;
	}
	else if (zero2 || inf1)
	{
		return num1;
	}

	if (num1.exp == num2.exp)
	{
		res.exp = num1.exp;
		res.mant = mant_sum_counter(num1, num2, round, mode);
		res.sign = num1.sign;
		if (num1.sign != num2.sign)
		{
			res.sign = num1.mant > num2.mant ? num1.sign : num2.sign;
		}
		return res;
	}

	res.exp = 0;
	if (num1.sign == num2.sign)
	{
		uint8_t len = define_len(num1.mant + num2.mant);
		uint8_t shift;

		if (mode == 'f')
		{
			shift = MANT32;
		}
		else
		{
			shift = MANT16;
		}
		if (len > shift)
		{
			res.exp += len - shift;
		}
		res.sign = num1.sign;
	}
	else
	{
		res.sign = num1.mant > num2.mant ? num1.sign : num2.sign;
	}

	if (num1.exp > num2.exp)
	{
		res.mant = mant_sum_counter(num1, num2, round, mode);
		res.exp += num1.exp;
	}
	else
	{
		res.mant = mant_sum_counter(num2, num1, round, mode);
		res.exp += num2.exp;
	}
	return res;
}

float_num subtractor(float_num num1, float_num num2, uint8_t round, char mode)
{
	num2.sign = !num2.sign;
	return (summator(num1, num2, round, mode));
}

int main(int argc, char* argv[])
{
	char numType, op;
	int8_t align;
	int32_t num1, num2;

	if (argc == 1)
	{
		fprintf(stderr, "NO ARGUMENTS");
		return ERROR_ARGUMENTS_INVALID;
	}

	if (argc == 5 || argc > 6 || argc < 4)
	{
		fprintf(stderr, "INCORRECT ARGUMENT AMOUNT");
		return ERROR_ARGUMENTS_INVALID;
	}
	int t = sscanf(argv[1], "%c", &numType);
	t += sscanf(argv[2], "%hhi", &align);
	t += sscanf(argv[3], "%x", &num1);
	if (argc - 1 == 5)
	{
		t += sscanf(argv[4], "%c", &op);
		t += sscanf(argv[5], "%x", &num2);
	}

	if (numType != 'f' && numType != 'h')
	{
		fprintf(stderr, "INCORRECT FORMAT");
		return ERROR_ARGUMENTS_INVALID;
	}
	else if (align > 3 || align < 0)
	{
		fprintf(stderr, "INCORRECT ROUNDING");
		return ERROR_ARGUMENTS_INVALID;
	}

	float_num res;
	float_num n1;
	float_num n2;

	if (t == 3)
	{
		out(converter(num1, numType), numType, align);
	}
	else
	{
		n1 = converter(num1, numType);
		n2 = converter(num2, numType);
		switch (op)
		{
		case '*':
			res = multiplicator(n1, n2, align, numType);
			break;
		case '/':
			res = divisor(n1, n2, align, numType);
			break;
		case '+':
			res = summator(n1, n2, align, numType);
			break;
		case '-':
			res = subtractor(n1, n2, align, numType);
			break;
		default:
			fprintf(stderr, "INCORRECT OPERATION");
			return ERROR_ARGUMENTS_INVALID;
		}
		out(res, numType, align);
	}

	return SUCCESS;
}
