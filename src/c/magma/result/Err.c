#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Err<T, X> {
	X error;
	impl Result<T, X> {
		void findValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
			return value;
		}
		void findError(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
			return value;
		}
		void and(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
			return value;
		}
		void mapValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
			return value;
		}
		void flatMapValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
			return value;
		}
	}
};