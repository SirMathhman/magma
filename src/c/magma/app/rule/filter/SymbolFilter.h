#include "java/util/function/Predicate.h"
#include "java/util/stream/IntStream.h"
struct SymbolFilter implements Predicate<String>{
	boolean test(String input){
		if(input.isEmpty())return false;
		return IntStream.range(0, input.length()).allMatch(()->{
			var c=input.charAt(index);
			return Character.isLetter(c) || c == '_'' || (index != 0 && Character.isDigit(c));
		});
	}
}
