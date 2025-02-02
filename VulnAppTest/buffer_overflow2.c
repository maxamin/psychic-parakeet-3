#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define A 64
#define B 128

int main(void) {
    char bof[A];
    char buf[sizeof(int)];

    int index;
    memset(bof, 0, A);

    if (!fgets(buf, sizeof(int), stdin))
    {
        // reading input failed, give up:
        return 1;
    }

    index = atoi(buf);
    bof[index] = 90;
    printf("%d", bof[index]);
}