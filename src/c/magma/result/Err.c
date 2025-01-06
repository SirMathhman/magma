#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Err<T, X> {
	X error;
	impl Result<T, X> {
		void findValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
		}
		void findError(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
		}
		void and(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
		}
		void mapValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
		}
		void flatMapValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>) __ref__;
		}
	}
};