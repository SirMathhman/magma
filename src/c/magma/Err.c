#include "temp.h"
struct Err<T, X> implements Result<T, X> {private final X error;
	void temp(){this.error = error;}

    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(this.error);}

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onErr.apply(this.error);}

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return new Err<>(this.error);
	}
};