#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define BUFFER_SIZE 1024

template <typename T>
struct Option {
    T value;
    bool is_some;

    static Option<T> Some(T val) {
        return Option<T>{val, true};
    }

    static Option<T> None() {
        return Option<T>{T(), false};
    }

    bool has_value() const {
        return is_some;
    }

    T unwrap() const {
        return value;
    }
};

enum ResultVariant {
    Ok,
    Err
};

template <typename T, typename X>
struct Result {
    ResultVariant variant;
    T value;
    X error;

    static Result<T, X> Ok(T value) {
        return Result<T, X>{Ok, value, X()};
    }

    static Result<T, X> Err(X error) {
        return Result<T, X>{Err, T(), error};
    }
};

struct String {
    size_t length;
    char* data;

    static String fromCString(char* data) {
        size_t length = strlen(data);

        char* allocated = new char[length + 1];
        memcpy(allocated, data, length);

        return String{length, allocated};
    }

    void drop() {
        free(data);
    }
};

namespace Files {
}

int main() {
    // Open the source file in read mode
    FILE *sourceFile = fopen("./main.mgs", "r");
    Option<FILE*> sourceOption = sourceFile ? Option<FILE*>::Some(sourceFile) : Option<FILE*>::None();
    if (!sourceOption.has_value()) {
        perror("Error opening source file");
        return EXIT_FAILURE;
    }

    // Determine the size of the source file
    fseek(sourceFile, 0, SEEK_END);
    long fileSize = ftell(sourceFile);
    rewind(sourceFile);

    // Allocate memory for the buffer
    char *buffer = (char *)malloc(fileSize + 1);
    Option<char*> bufferOption = buffer ? Option<char*>::Some(buffer) : Option<char*>::None();
    if (!bufferOption.has_value()) {
        perror("Memory allocation failed");
        fclose(sourceFile);
        return EXIT_FAILURE;
    }

    // Read the entire file into the buffer
    fread(buffer, 1, fileSize, sourceFile);
    buffer[fileSize] = '\0'; // Null-terminate the string

    // Open the destination file in write mode
    FILE *destinationFile = fopen("./main.cpp", "w");
    Option<FILE*> destOption = destinationFile ? Option<FILE*>::Some(destinationFile) : Option<FILE*>::None();
    if (!destOption.has_value()) {
        perror("Error opening destination file");
        free(buffer);
        fclose(sourceFile);
        return EXIT_FAILURE;
    }

    // Write the string to the destination file
    fputs(buffer, destinationFile);

    printf("Content successfully copied from ./main.mgs to ./main.cpp\n");

    // Free allocated memory and close both files
    free(buffer);
    fclose(sourceFile);
    fclose(destinationFile);

    return EXIT_SUCCESS;
}
