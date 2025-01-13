#include "temp.h"
struct Result<T, X> {
	X> mapValue(R> mapper);
	R match(R> onOkR> onErr);
	X> flatMapValue(X>> mapper);
};