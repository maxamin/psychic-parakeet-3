/* see copyright notice in VWSLang.h */

#include <iostream>
#include <fstream>
#include <set>
#include <string>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <vwsl_vm.h>

using namespace std;

int main(int argc, char *argv[]) {
	char *buffer = NULL;
   	int string_size, read_size;
	FILE *fPtr = NULL;
	vwslVM vm;

	fPtr = fopen(argv[1], "r");
	if(fPtr)
	{
		// Seek the last byte of the file
       	fseek(fPtr, 0, SEEK_END);
       	// Offset from the first to the last byte, or in other words, filesize
       	string_size = ftell(fPtr);
       	// go back to the start of the file
       	rewind(fPtr);

		// Allocate a string that can hold it all
		buffer = (char*) malloc(sizeof(char) * (string_size + 1) );

		// Read it all in one operation
		read_size = fread(buffer, sizeof(char), string_size, fPtr);

		// fread doesn't set it so put a \0 in the last position
		// and buffer is now officially a string
		buffer[string_size] = '\0';

		if (string_size != read_size)
		{
			// Something went wrong, throw away the memory and set
			// the buffer to NULL
			free(buffer);
			buffer = NULL;
		}

		// Always remember to close the file.
		fclose(fPtr);
	}
	
	vm.runCode(buffer);
	
	return 0;
}
