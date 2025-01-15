#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <locale.h>

typedef const char* string;

enum ResultType {
    Ok,
    Err
}

typedef union ResultValue {
    FILE* file;
    errno_t error;
} ResultValue;

typedef struct Result {
    ResultType type;
    ResultValue value;
} Result;

Result new_Ok(FILE* file) {
    Result this;
    this.type = Ok;
    this.value.file = file; // Initialize the file in the union
    return this;
}

Result new_Err(errno_t error) {
    Result this;
    this.type = Err;
    this.value.error = error; // Initialize the error in the union
    return this;
}

Result openFileSecurely(string path, string mode) {
    FILE* file;
    errno_t error = fopen_s(&file, path, mode);
    if (error == 0) {
        return new_Ok(file);
    } else {
        return new_Err(error);
    }
}

int main() {
    Result result = openFileSecurely("./Main.mgs", "rb");
    if (result.type == Ok) {
        FILE* file = result.value.file;
        fseek(file, 0, SEEK_END);
        long length = ftell(file);
        fseek(file, 0, SEEK_SET);

        char* content = (char*)malloc(sizeof(char) * (length + 1));
        if (!content) {
            fprintf(stderr, "Memory allocation failed.\n");
            fclose(file);
            return -1;
        }

        size_t bytesRead = fread(content, sizeof(char), length, file);
        if (bytesRead != length) {
            fprintf(stderr, "Failed to read file content.\n");
            free(content);
            fclose(file);
            return -1;
        }
        content[length] = '\0';

        printf("%s", content);

        free(content);
        fclose(file);
        return 0;
    } else {
        char message[256];
        if (strerror_s(message, sizeof(message), result.value.error) == 0) {
            fprintf(stderr, "Error opening file: %s\n", message);
        } else {
            fprintf(stderr, "Unknown error occurred.\n");
        }
        return -1;
    }
}
