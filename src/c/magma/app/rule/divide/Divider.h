#include "../../../../magma/api/result/Result.h"
#include "../../../../magma/app/error/CompileError.h"
#include "../../../../java/util/List.h"
struct Divider{
	String merge(String current, String value);
	Result<List<String>, CompileError> divide(String input);
}
