import java.util.function.Predicate;
import java.util.stream.IntStream;
public struct SymbolFilter implements Predicate<String> {
	boolean test=boolean test(String input){
		return IntStream.range(0, input.length()).allMatch(index ->{
		final var c=input.charAt(index);
		return Character.isLetter(c) || c == '_'' || (index != 0 && Character.isDigit(c));
	});
	};
}