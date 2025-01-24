import java.util.stream.IntStream;struct NumberFilter{
	boolean allDigits(any* _ref_, String input){
		return IntStream.range(0, input.length()).mapToObj(input::charAt).allMatch(Character::isDigit);
	}
	boolean test(any* _ref_, String input){
		if(input.startsWith("-")){
			return allDigits(input.substring(1));
		}
		return allDigits(input);
	}
	Predicate<String> Predicate(any* _ref_){
		return Predicate.new();
	}
}