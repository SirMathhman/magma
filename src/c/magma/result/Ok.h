#include "../../magma/option/Option.h"
#include "../../magma/option/Some.h"
#include "../../java/util/Optional.h"
struct Ok<T, X>(T value) implements Result<T, X> {};