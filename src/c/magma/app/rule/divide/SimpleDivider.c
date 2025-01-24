#include "magma/api/result/Ok.h"
#include "magma/api/result/Result.h"
#include "magma/app/error/CompileError.h"
#include "java/util/Arrays.h"
#include "java/util/List.h"
#include "java/util/regex/Pattern.h"
struct SimpleDivider implements Divider{
	String delimiter;
	public SimpleDivider(String delimiter){
		this.delimiter =delimiter;
	}
	String merge(String current, String value){
		return current+this.delimiter + value;
	}
	Result<List<String>, CompileError> divide(String input){
		return new Ok<>(Arrays.stream(input.split(Pattern.quote(this.delimiter))).toList());
	}
}
