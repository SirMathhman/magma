#include <stdio.h>
#include <stdlib.h>

#define BUFFER_SIZE 1024

int main() {
    FILE *sourceFile, *destinationFile;
    char *buffer;
    long fileSize;

    // Open the source file in read mode
    sourceFile = fopen("./main.mgs", "r");
    if (sourceFile == NULL) {
        perror("Error opening source file");
        return EXIT_FAILURE;
    }

    // Determine the size of the source file
    fseek(sourceFile, 0, SEEK_END);
    fileSize = ftell(sourceFile);
    rewind(sourceFile);

    // Allocate memory for the buffer
    buffer = (char *)malloc(fileSize + 1);
    if (buffer == NULL) {
        perror("Memory allocation failed");
        fclose(sourceFile);
        return EXIT_FAILURE;
    }

    // Read the entire file into the buffer
    fread(buffer, 1, fileSize, sourceFile);
    buffer[fileSize] = '\0'; // Null-terminate the string

    // Open the destination file in write mode
    destinationFile = fopen("./main.c", "w");
    if (destinationFile == NULL) {
        perror("Error opening destination file");
        free(buffer);
        fclose(sourceFile);
        return EXIT_FAILURE;
    }

    // Write the string to the destination file
    fputs(buffer, destinationFile);

    printf("Content successfully copied from ./main.mgs to ./main.c\n");

    // Free allocated memory and close both files
    free(buffer);
    fclose(sourceFile);
    fclose(destinationFile);

    return EXIT_SUCCESS;
}WVC16t