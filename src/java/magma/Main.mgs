#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <locale.h>

typedef const char* string;

typedef enum ResultType {
    Ok,
    Err
} ResultType;

typedef union ResultValue {
    FILE* value;
    errno_t error;
} ResultValue;

typedef struct Result {
    ResultType type;
    ResultValue value;
} Result;

Result new_Ok(FILE* value) {
    Result this;
    this.type = Ok;
    return this;
}

Result new_Err(errno_t error) {
    Result this;
    this.type = Err;
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

int main(){
    Result result = openFileSecurely("./Main.mgs", "r");
    if(result.type == Ok) {
        fclose(result.value.value);
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