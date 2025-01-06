#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Ok<T, X> {
	T value;
	impl Result<T, X> {
		void findValue(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>) __ref__;
			return value;
		}
		void findError(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>) __ref__;
			return value;
		}
		void and(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>) __ref__;
			return value;
		}
		void mapValue(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>) __ref__;
			return value;
		}
		void flatMapValue(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>) __ref__;
			return value;
		}
	}
};