#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
struct Ok<T, X>(T value) {
	void findValue(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return Optional.of(this.value);
	}
	void findError(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return Optional.empty();
	}
	void and(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		caller();
	}
	void mapValue(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return temp();
	}
	void flatMapValue(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return mapper.apply(this.value);
	}
	void match(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return onOk.apply(this.value);
	}
	void isOk(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return true;
	}
	void mapErr(void* __ref__){
		struct Ok<T, X>(T value)* this = (struct Ok<T, X>(T value)*) __ref__;
		return temp();
	}
};