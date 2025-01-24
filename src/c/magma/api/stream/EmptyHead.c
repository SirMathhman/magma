#include "../../../magma/api/option/None.h"
#include "../../../magma/api/option/Option.h"
struct EmptyHead<T> implements Head<T>{
	Option<T> next(){
		return new None<>();
	}
}
