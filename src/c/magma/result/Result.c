#include "magma/Tuple.h";
#include "java/util/Optional.h";
#include "java/util/function/Function.h";
#include "java/util/function/Supplier.h";
struct Result<T, X>  {
	Optional<T> findValue();
	Optional<X> findError();
	X>> otherSupplier);
	R> mapper);
	X>> mapper);
	R> onErr);
	R> mapper);
	boolean isOk();
};