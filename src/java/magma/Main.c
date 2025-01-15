#include <stdio.h>

typedef const char* string;

typedef struct Result {
} Result;

Result Ok(FILE* value) {
    Result this;
    return this;
}

Result Err(errno_t error) {
    Result this;
    return this;
}

Result openFileSecurely(string path, string mode) {
    FILE* file;
    errno_t error = fopen_s(&file, path, mode);
    if (error == 0) {
        return Ok(file);
    } else {
        return Err(error);
    }
}

int main(){
    return 0;
}