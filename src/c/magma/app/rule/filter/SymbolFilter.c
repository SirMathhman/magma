import java.util.function.Predicate;
import java.util.stream.IntStream;
 struct SymbolFilter implements Predicate<String> {
	@Override
 boolean test( String input){
		return IntStream.range(0, input.length()).allMatch(index ->{
			final var c=input.charAt(index);
			return Character.isLetter(c) || c == '_'' || (index != 0 && Character.isDigit(c));
		});
	}
}

