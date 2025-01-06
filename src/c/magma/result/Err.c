#include "../../magma/option/Option.h"
#include "../../magma/option/Some.h"
struct Err<T, X>(X error) implements Result<T, X> {};