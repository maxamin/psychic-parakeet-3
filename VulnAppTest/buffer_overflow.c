// gcc -Wno-stack-protector -no-pie -z execstack loophole.c -o loophole

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define BUFSIZE 64

char buf1[BUFSIZE], buf2[BUFSIZE];

void bof()
{
	int i1, i2, i;
	int byte1, byte2;
	char buf[BUFSIZE];
	printf("Buffer starts at: %p\n", buf);
    fflush(stdout);

	memset(buf, 0, BUFSIZE);
	byte1 = fread(buf1, 1, BUFSIZE, stdin);
	byte2 = fread(buf2, 1, BUFSIZE, stdin);

	buf1[BUFSIZE-1] = 0;
	buf2[BUFSIZE-1] = 0;

	if(byte1 != byte2)
	{
		printf("Reading different number of bytes from both inputs!\n");
		return;
	}

	for(i = 0; i < byte1 + byte2; i++)
	{
		i1 = (i % 2) ? BUFSIZE - 1 : i / 2;
		i2 = (i % 2) ? i / 2 : BUFSIZE - 1;
		buf[i] = buf1[i1] + buf2[i2];
	}

}

int main(void)
{

  bof();

	return 0;
}
