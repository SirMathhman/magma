#include "temp.h"
public interface Result<T, X> {
    <R> Result<R, X> mapValue(Function<T, R> mapper);

    <R> R match(Function<T, R> onOk, Function<X, R> onErr);

    <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);
}