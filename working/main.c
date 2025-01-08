#include <stdio.h>
#include <stdlib.h>

int main() {
    // Define the file path
    const char *filePath = "./text.txt";

    // File pointer
    FILE *file;

    // Open the file securely using fopen_s
    if (fopen_s(&file, filePath, "r") != 0) {
        perror("Error opening file");
        return EXIT_FAILURE;
    }

    // Seek to the end to get the file size
    fseek(file, 0, SEEK_END);
    long fileSize = ftell(file);
    rewind(file); // Reset the file pointer to the beginning

    // Allocate a buffer to store the file content
    char *buffer = (char *)malloc(fileSize + 1); // +1 for the null terminator
    if (buffer == NULL) {
        perror("Error allocating memory");
        fclose(file);
        return EXIT_FAILURE;
    }

    // Read the file into the buffer
    size_t bytesRead = fread(buffer, 1, fileSize, file);
    buffer[bytesRead] = '\0'; // Null-terminate the buffer

    // Print the buffer content to the console
    printf("%s", buffer);

    // Free the buffer and close the file
    free(buffer);
    fclose(file);

    return EXIT_SUCCESS;
}
