#include "../../../magma/api/option/None.h"
#include "../../../magma/api/option/Option.h"
#include "../../../magma/api/option/Some.h"
struct SingleHead<T> implements Head<T>{
	T value;
	boolean retrieved;
	public SingleHead(T value){
		this.value = value;
	}
	Option<T> next(){
		if(this.retrieved)return new None<>();
		this.retrieved = true;
		return new Some<>(this.value);
	}
}
