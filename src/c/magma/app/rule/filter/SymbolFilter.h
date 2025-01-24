import java.util.stream.IntStream;struct SymbolFilter implements Predicate<String>{
	struct Table{
		boolean test(String input){
			if(input.isEmpty())return false;
			return IntStream.range(0, input.length()).allMatch(()->{
				var c=input.charAt(index);
				return Character.isLetter(c) || c == '_'' || (index != 0 && Character.isDigit(c));
			});
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}