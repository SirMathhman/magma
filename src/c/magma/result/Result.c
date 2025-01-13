#include "temp.h"
#include "temp.h"
struct Result<T, X> {
	Result<R, X> mapValue((T => R) mapper);
	R match((T => R) onOk, (X => R) onErr);
	Result<R, X> flatMapValue((T => Result<R, X>) mapper);
	Optional<T> findValue();
};