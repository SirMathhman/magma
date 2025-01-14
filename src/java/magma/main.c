#include <stdlib.h>
#include <stdio.h>
enum Result_Type {
	Ok,
	Err
};
struct Result {
	enum Result_Type type;
};
int main() {
    return 0;
}