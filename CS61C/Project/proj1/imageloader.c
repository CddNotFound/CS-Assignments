/************************************************************************
**
** NAME:        imageloader.c
**
** DESCRIPTION: CS61C Fall 2020 Project 1
**
** AUTHOR:      Dan Garcia  -  University of California at Berkeley
**              Copyright (C) Dan Garcia, 2020. All rights reserved.
**              Justin Yokota - Starter Code
**				YOUR NAME HERE
**
**
** DATE:        2020-08-15
**
**************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <string.h>
#include "imageloader.h"

//Opens a .ppm P3 image file, and constructs an Image object. 
//You may find the function fscanf useful.
//Make sure that you close the file with fclose before returning.
Image *readData(char *filename) 
{
	//YOUR CODE HERE

	Image *v = (Image *)malloc(sizeof(Image));

	if (v == NULL) {
		exit(-1);
	}

	FILE *fp = fopen(filename, "r");
	char type[20];
	int rg, row, col;
	fscanf(fp, "%s", type);
	fscanf(fp, "%u %u", &col, &row);
	fscanf(fp, "%u", &rg);

	v -> cols = col, v -> rows = row;
	v -> image = (Color **)malloc(row * sizeof(Color *));
	if (v -> image == NULL) {
		exit(-1);
	}
	
	int x, y, z;
	for (int i = 0; i < row; i++) {
		v -> image[i] = (Color *)malloc(col * sizeof(Color));
		if (v -> image[i] == NULL) {
			exit(-1);
		}
		for (int j = 0; j < col; j++) {
			fscanf(fp, "%u %u %u", &x, &y, &z);
			v -> image[i][j] = (Color){x, y, z};
		}
	}
	fclose(fp);

	return v;
}

//Given an image, prints to stdout (e.g. with printf) a .ppm P3 file with the image's data.
void writeData(Image *image)
{
	//YOUR CODE HERE

	printf("P3\n");
	printf("%u %u\n", image -> cols, image -> rows);
	printf("255\n");
	for (int i = 0; i < image -> rows; i++) {
		for (int j = 0; j < image -> cols; j++) {
			if (j)
				printf("   ");
			Color *tmp = &(image -> image[i][j]);
			printf("%3u %3u %3u", tmp -> R, tmp -> G, tmp -> B);
		}
		printf("\n");
	}
}

//Frees an image
void freeImage(Image *image)
{
	//YOUR CODE HERE

	if (image != NULL) {
		if (image -> image != NULL) {
			free(image -> image);
			image -> image = NULL;
		}
		free(image);
		image = NULL;
	}
}