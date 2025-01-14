#include <stdlib.h>
#include <stdio.h>
enum Result_Type {
	Ok,
	Err
};
union Result_Value {
	FILE* okValue;
	errno_t errValue;
};
struct Result_Result {
	enum Result_Type type;
	union Result_Value value;
};
int main() {
    return 0;
}