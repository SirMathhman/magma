import java.util.stream.IntStream;struct SymbolFilter{
	boolean test(any* _ref_, String input){
		if(input.isEmpty())return false;
		return IntStream.range(0, input.length()).allMatch(()->{
			var c=input.charAt(index);
			return Character.isLetter(c) || c == '_'' || (index != 0 && Character.isDigit(c));
		});
	}
	Predicate<String> Predicate(any* _ref_){
		return Predicate.new();
	}
}