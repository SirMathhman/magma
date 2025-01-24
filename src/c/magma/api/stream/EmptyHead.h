#include "../../../java/util/Optional.h"
struct EmptyHead<T> implements Head<T>{
	Optional<T> next(){
		return Optional.empty();
	}
}
