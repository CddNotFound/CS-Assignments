#include "transpose.h"

/* The naive transpose function as a reference. */
void transpose_naive(int n, int blocksize, int *dst, int *src) {
    for (int x = 0; x < n; x++) {
        for (int y = 0; y < n; y++) {
            dst[y + x * n] = src[x + y * n];
        }
    }
}

/* Implement cache blocking below. You should NOT assume that n is a
* multiple of the block size. */
#define min(X, Y) ((X) < (Y) ? (X) : (Y))
void transpose_blocking(int n, int blocksize, int *dst, int *src) {
    // YOUR CODE HERE
    for (int i = 0; i <= n / blocksize; i++)
        for (int j = 0; j <= n / blocksize; j++) {
            for (int x = i * blocksize; x < (i + 1) * blocksize && x < n; x++)
                for (int y = j * blocksize; y < (j + 1) * blocksize && y < n; y++)
                    dst[y + x * n] = src[x + y * n];
        }
}
