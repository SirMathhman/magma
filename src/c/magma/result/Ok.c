#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
struct Ok<T, X>(T value) {
	void findValue(void* __ref__){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=Node[value=Node[value=Optional].of](Node[value=Node[value=this].value])];
	}
	void findError(void* __ref__){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void and(void* __ref__, Supplier<Result<R, X>> otherSupplier){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		caller();
	}
	void mapValue(void* __ref__, Function<T, R> mapper){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=temp()];
	}
	void flatMapValue(void* __ref__, Function<T, Result<R, X>> mapper){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=Node[value=Node[value=mapper].apply](Node[value=Node[value=this].value])];
	}
	void match(void* __ref__, Function<T, R> onOk, Function<X, R> onErr){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=Node[value=Node[value=onOk].apply](Node[value=Node[value=this].value])];
	}
	void isOk(void* __ref__){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=true];
	}
	void mapErr(void* __ref__, Function<X, R> mapper){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return Node[value=temp()];
	}
};