#include <math.h>
#include <stdint.h>
#include <stdio.h>

uint32_t fact(uint16_t num, uint32_t border)
{
	if (num == 0)
	{
		return 1;
	}
	uint64_t res = 1;
	for (uint16_t i = 1; i < num; i++)
	{
		res *= i;
		res %= border;
	}
	res *= num;
	res %= border;
	return res;
}

uint32_t factOpt(uint16_t num, uint32_t border, uint32_t prev)
{
	if (num == 0)
	{
		return 1;
	}

	uint64_t res = prev;
	res *= num;
	res %= border;

	return res;
}



int main(void)
{
	int32_t start;
	int32_t end;
	int8_t align;

	scanf("%u %u %hhi", &start, &end, &align);

	if (start < 0 || end < 0 || start > 65535 || end > 65535)
	{
		fprintf(stderr, "Incorrect border");
		return 1;
	}
	uint16_t n_start = start;
	uint16_t n_end = end;

	uint8_t max_left = 0;
	uint8_t max_right = 0;

	if (n_start > n_end)
	{
		uint16_t num = n_start;
		if (num > 12)
		{
			max_right = 1;
			uint32_t res = fact(n_start, INT32_MAX);
			for (uint16_t start = n_start; start != end; start++) {
				uint8_t len = res == 1 ? 1 : floor(log10(res));
				if (len == 10) {
					max_right = 10;
					break;
				}
				else if (max_right < len)
				{
					max_right = len;
				}
				res = factOpt(start, INT32_MAX, res);
			}
		}
		else
		{
			uint32_t fact_num = fact(num, INT32_MAX);
			while (fact_num > 0)
			{
				max_right++;
				fact_num /= 10;
			}
		}
		while (num > 0)
		{
			num /= 10;
			max_left++;
		}
	}
	else
	{
		uint16_t num = n_end;
		if (num > 12)
		{
			max_right = 1;
			uint32_t res = fact(n_start, INT32_MAX);
			for (uint16_t start = n_start; start != end; start++) {
				uint8_t len = res == 1 ? 1 : floor(log10(res));
				if (len == 10) {
					max_right = 10;
					break;
				}
				else if (max_right < len) {
					max_right = len;
				}
				res = factOpt(start, INT32_MAX, res);
			}
		}
		else
		{
			uint32_t fact_num = fact(num, INT32_MAX);
			while (fact_num > 0)
			{
				max_right++;
				fact_num /= 10;
			}
		}
		while (num > 0)
		{
			num /= 10;
			max_left++;
		}
	}
	if (n_start == 0 && n_end == 0)
	{
		max_left = 1;
	}
	if (max_right == 1)
	{
		max_right++;
	}

	char *dash = "-----------------";

	char *head_spec;
	char *spec;
	if (align == 1)
	{
		head_spec = "| %*s | %*s |\n";
		spec = "| %*hu | %*u |\n";
	}
	else if (align == -1)
	{
		head_spec = "| %-*s | %-*s |\n";
		spec = "| %-*hu | %-*u |\n";
	}

	printf("+-%.*s-+-%.*s-+\n", max_left, dash, max_right+1, dash);
	// HEAD
	if (align == 0)
	{
		printf("| %*s%s%*s | %*s%s%*s |\n", max_left / 2, "", "n", (max_left - 1) / 2, "", (max_right) / 2, "", "n!", (max_right-1) / 2, "");
	}
	else
	{
		printf(head_spec, max_left, "n", max_right, "n!", 2 - 1 - max_right, "");
	}

	printf("+-%.*s-+-%.*s-+\n", max_left, dash, max_right+1, dash);

	// BODY 1 SIZE
	if (n_start == n_end)
	{
		if (align == 0)
		{
			uint8_t len_left = floor(log10(n_start));
			len_left++;

			uint32_t right = fact(n_start, INT32_MAX);
			uint8_t len_right = right == 1 ? 1 : floor(log10(right));

			printf("| %*s%hu%*s | ", (max_left - len_left + 1) / 2, "", n_start, (max_left - len_left) / 2, "");
			printf("%*s%u%*s |\n", (max_right - len_right + 1) / 2, "", right, (max_right - len_right) / 2, "");
		}
		else
		{
			printf(spec, max_left, n_start, max_right, fact(n_start, INT32_MAX));
		}
	}

	uint32_t res = fact(n_start, INT32_MAX);
	// BODY >1 SIZE
	while (n_start != n_end)
	{
		if (align == 0)
		{
			uint8_t len_left = floor(log10(n_start));
			len_left++;

			uint8_t len_right = floor(log10(res));
			len_right++;
			printf("| %*s%hu%*s | ", (max_left - len_left + 1) / 2, "", n_start, (max_left - len_left) / 2, "");
			printf("%*s%u%*s |\n", (max_right - len_right + 1) / 2, "", res, (max_right - len_right) / 2, "");
			n_start++;
			res = factOpt(n_start, INT32_MAX, res);
			if (n_start == n_end)
			{
				len_left = floor(log10(n_start));
				len_left++;

				res = fact(n_start, INT32_MAX);
				len_right = floor(log10(res));
				len_right++;

				printf("| %*s%hu%*s | ", (max_left - len_left + 1) / 2, "", n_start, (max_left - len_left) / 2, "");
				printf("%*s%u%*s |\n", (max_right - len_right + 1) / 2, "", res, (max_right - len_right) / 2, "");
			}

		}
		else
		{
			printf(spec, max_left, n_start, max_right, res);
			n_start++;
			res = factOpt(n_start, INT32_MAX, res);
			if (n_start == n_end)
			{
				printf(spec, max_left, n_start, max_right, res);
			}
		}
	}

	printf("+-%.*s-+-%.*s-+\n", max_left, dash, max_right, dash);

	return 0;
}
