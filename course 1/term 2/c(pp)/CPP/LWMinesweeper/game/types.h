#ifndef TYPES_H
#define TYPES_H

typedef struct position
{
	unsigned int col;
	unsigned int row;
} position;

typedef struct specs
{
	unsigned int height;
	unsigned int width;
	unsigned int mines_amount;
} specs;
#endif	  // TYPES_H
