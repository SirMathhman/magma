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
struct Result_Result new_Result_Ok(FILE* value){
	struct Result_Result this;
	this.type = Ok;
	this.value.okValue = value;
	return this;
}
struct Result_Result new_Result_Err(errno_t error){
	struct Result_Result this;
	this.type = Err;
	this.value.errValue = error;
	return this;
}
int main(){
	return 0;
}
