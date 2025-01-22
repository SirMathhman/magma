import java.util.function.Predicate;
import java.util.stream.IntStream;

boolean test(String input){
	if(input.isEmpty())return false;
	return IntStream.range(0, input.length()).allMatch(auto _lambda37_(auto index){
		const auto c=input.charAt(index);
		return Character.isLetter(c) || c == '_'' || (index != 0 && Character.isDigit(c));
	});
}
struct SymbolFilter implements Predicate<String> {
}

