/************************************************************************
**
** NAME:        gameoflife.c
**
** DESCRIPTION: CS61C Fall 2020 Project 1
**
** AUTHOR:      Justin Yokota - Starter Code
**				YOUR NAME HERE
**
**
** DATE:        2020-08-23
**
**************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include "imageloader.h"

//Determines what color the cell at the given row/col should be. This function allocates space for a new Color.
//Note that you will need to read the eight neighbors of the cell in question. The grid "wraps", so we treat the top row as adjacent to the bottom row
//and the left column as adjacent to the right column.

const int dx[9] = {0, 1, 0, -1, 0, 1, 1, -1, -1};
const int dy[9] = {0, 0, 1, 0, -1, 1, -1, 1, -1};

Color *evaluateOneCell(Image *image, int row, int col, uint32_t rule)
{
	//YOUR CODE HERE

	Color *newColor = (Color *)malloc(sizeof(Color));
	
	if (newColor == NULL) {
		exit(-1);
	}

	int n = image -> rows, m = image -> cols;
	int cnt[24] = {0};
	int self = image -> image[row][col].R | (image -> image[row][col].G << 8) | (image -> image[row][col].B << 16);
	for (int i = 1; i <= 8; i++) {
		int x = row + dx[i], y = col + dy[i];
		if (x >= n) x -= n;
		if (x < 0) x += n;
		if (y >= m) y -= m;
		if (y < 0) y += m;

		for (int j = 0; j < 8; j++) {
			if (image -> image[x][y].R) ++cnt[j];
			if (image -> image[x][y].G) ++cnt[j + 8];
			if (image -> image[x][y].B) ++cnt[j + 16];
		}
	}

	int tmp = 0;
	for (int i = 0; i < 24; i++)
		if (((self >> i & 1) && (rule >> (cnt[i] + 9) & 1)) || (!(self >> i & 1) && (rule >> cnt[i] & 1))) {
			tmp |= 1 << i;
		}

	*newColor = (Color){tmp % (1 << 8), (tmp >> 8) % (1 << 8), tmp >> 16};
		
	return newColor;
}

//The main body of Life; given an image and a rule, computes one iteration of the Game of Life.
//You should be able to copy most of this from steganography.c
Image *life(Image *image, uint32_t rule)
{
	//YOUR CODE HERE

	Image *v = (Image *)malloc(sizeof(Image));
	if (v == NULL) {
		exit(-1);
	}
	v -> image = (Color **)malloc(sizeof(Color *) * image -> rows);
	if (v -> image == NULL) {
		exit(-1);
	}

	v -> rows = image -> rows, v -> cols = image -> cols;
	for (int i = 0; i < image -> rows; i++) {
		v -> image[i] = (Color *)malloc(sizeof(Color) * image -> cols);
		if (v -> image[i] == NULL) {
			exit(-1);
		}
		for (int j = 0; j < image -> cols; j++) {
			Color *tmp = evaluateOneCell(image, i, j, rule);
			v -> image[i][j] = *tmp;
			free(tmp);
		}
	}

	return v;
}

/*
Loads a .ppm from a file, computes the next iteration of the game of life, then prints to stdout the new image.

argc stores the number of arguments.
argv stores a list of arguments. Here is the expected input:
argv[0] will store the name of the program (this happens automatically).
argv[1] should contain a filename, containing a .ppm.
argv[2] should contain a hexadecimal number (such as 0x1808). Note that this will be a string.
You may find the function strtol useful for this conversion.
If the input is not correct, a malloc fails, or any other error occurs, you should exit with code -1.
Otherwise, you should return from main with code 0.
Make sure to free all memory before returning!

You may find it useful to copy the code from steganography.c, to start.
*/
int main(int argc, char **argv)
{
	//YOUR CODE HERE

	if (argc <= 2 || argc >= 4) {
		printf("usage: ./gameOfLife filename rule\nfilename is an ASCII PPM file (type P3) with maximum value 255.\nrule is a hex number beginning with 0x; Life is 0x1808.");
		exit(-1);
	}

	int rule = strtol(argv[2], NULL, 16);
	Image *image = readData(argv[1]); 
	Image *info = life(image, rule);

	writeData(info);

	freeImage(image);
	freeImage(info);

	return 0;
}
