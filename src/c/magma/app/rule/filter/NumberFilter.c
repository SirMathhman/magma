import java.util.function.Predicate;
import java.util.stream.IntStream;
public struct NumberFilter implements Predicate<String> {
	((String) => boolean) allDigits=boolean allDigits(String input){
		return IntStream.range(0, input.length()).mapToObj(input::charAt).allMatch(Character::isDigit);
	};
	((String) => boolean) test=boolean test(String input){
		if(input.startsWith("-")){
		return allDigits(input.substring(1));
	}
		return allDigits(input);
	};
}