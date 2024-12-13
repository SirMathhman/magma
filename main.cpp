#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <cerrno> // For errno and strerror

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
        return Result<T, X>{ResultVariant::Ok, value, X()};
    }

    static Result<T, X> Err(X error) {
        return Result<T, X>{ResultVariant::Err, T(), error};
    }

    bool is_ok() const {
        return variant == Ok;
    }

    T unwrap() const {
        return value;
    }

    X unwrap_err() const {
        return error;
    }
};

struct String {
    size_t length;
    char* data;

    static String fromCString(const char* data) {
        size_t length = strlen(data);

        char* allocated = new char[length]; // Allocate exactly 'length'
        memcpy(allocated, data, length);   // Copy only the data (no null terminator)

        return String{length, allocated};
    }

    static String withSize(size_t size) {
        char* allocated = new char[size];
        return String{size, allocated};
    }

    char* toCString() const {
        char* allocated = new char[length + 1]; // Allocate space for the null terminator
        memcpy(allocated, data, length);
        allocated[length] = '\0'; // Explicitly null-terminate the string

        return allocated;
    }

    void drop() {
        delete[] data; // Free the allocated memory
    }
};

namespace Files {
    Result<FILE*, int> open(String* path, String* mode) {
        char* pathString = path->toCString();
        char* modeString = mode->toCString();

        FILE* file = nullptr;
        int errorCode = fopen_s(&file, pathString, modeString);
        delete[] pathString;
        delete[] modeString;

        if (errorCode == 0 && file) {
            return Result<FILE*, int>::Ok(file);
        } else {
            return Result<FILE*, int>::Err(errorCode);
        }
    }
}

int main() {
    // Open the source file in read mode using Files::open
    String sourcePath = String::fromCString("./main.mgs");
    String readMode = String::fromCString("r");
    auto sourceFileResult = Files::open(&sourcePath, &readMode);
    sourcePath.drop();
    readMode.drop();

    if (!sourceFileResult.is_ok()) {
        int errorCode = sourceFileResult.unwrap_err();
        fprintf(stderr, "Error opening source file: %s\n", strerror(errorCode));
        return EXIT_FAILURE;
    }

    FILE* sourceFile = sourceFileResult.unwrap();

    // Determine the size of the source file
    fseek(sourceFile, 0, SEEK_END);
    long fileSize = ftell(sourceFile);
    rewind(sourceFile);

    // Create a String to hold the file content
    String buffer = String::withSize(fileSize);
    if (!buffer.data) {
        perror("Memory allocation failed");
        fclose(sourceFile);
        return EXIT_FAILURE;
    }

    // Read the entire file into the buffer
    fread(buffer.data, 1, fileSize, sourceFile);

    // Open the destination file in write mode using Files::open
    String destinationPath = String::fromCString("./main.cpp");
    String writeMode = String::fromCString("w");
    auto destinationFileResult = Files::open(&destinationPath, &writeMode);
    destinationPath.drop();
    writeMode.drop();

    if (!destinationFileResult.is_ok()) {
        int errorCode = destinationFileResult.unwrap_err();
        fprintf(stderr, "Error opening destination file: %s\n", strerror(errorCode));
        buffer.drop();
        fclose(sourceFile);
        return EXIT_FAILURE;
    }

    FILE* destinationFile = destinationFileResult.unwrap();

    // Write the string to the destination file
    fwrite(buffer.data, 1, buffer.length, destinationFile);

    printf("Content successfully copied from ./main.mgs to ./main.cpp\n");

    // Free allocated memory and close both files
    buffer.drop();
    fclose(sourceFile);
    fclose(destinationFile);

    return EXIT_SUCCESS;
}
