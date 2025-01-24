#include "./Divider.h"
struct Divider{
	String merge(String current, String value);
	Result<List<String>, CompileError> divide(String input);
}
