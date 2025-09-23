/************************************************************************
**
** NAME:        steganography.c
**
** DESCRIPTION: CS61C Fall 2020 Project 1
**
** AUTHOR:      Dan Garcia  -  University of California at Berkeley
**              Copyright (C) Dan Garcia, 2020. All rights reserved.
**				Justin Yokota - Starter Code
**				YOUR NAME HERE
**
** DATE:        2020-08-23
**
**************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include "imageloader.h"

//Determines what color the cell at the given row/col should be. This should not affect Image, and should allocate space for a new Color.
Color *evaluateOnePixel(Image *image, int row, int col)
{
	//YOUR CODE HERE

	Color *newColor = (Color *)malloc(sizeof(Color));
	
	if (newColor == NULL) {
		exit(-1);
	}

	Color *v = &(image -> image[row][col]);
	if (v -> B & 1) 
		*newColor = (Color){255, 255, 255};
	else
		*newColor = (Color){0, 0, 0};
	
	return newColor;
}

//Given an image, creates a new image extracting the LSB of the B channel.
Image *steganography(Image *image)
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
			Color *tmp = evaluateOnePixel(image, i, j);
			v -> image[i][j] = *tmp;
			free(tmp);
		}
	}

	return v;
}

/*
Loads a file of ppm P3 format from a file, and prints to stdout (e.g. with printf) a new image, 
where each pixel is black if the LSB of the B channel is 0, 
and white if the LSB of the B channel is 1.

argc stores the number of arguments.
argv stores a list of arguments. Here is the expected input:
argv[0] will store the name of the program (this happens automatically).
argv[1] should contain a filename, containing a file of ppm P3 format (not necessarily with .ppm file extension).
If the input is not correct, a malloc fails, or any other error occurs, you should exit with code -1.
Otherwise, you should return from main with code 0.
Make sure to free all memory before returning!
*/
int main(int argc, char **argv)
{
	//YOUR CODE HERE

	// FILE *fp = fopen(argv[1], "r");

	Image *image = readData(argv[1]); 
	Image *info = steganography(image);

	writeData(info);

	freeImage(image);
	freeImage(info);

	return 0;
}
