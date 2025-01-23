import java.util.stream.IntStream;struct NumberFilter implements Predicate<String>{
	boolean allDigits(String input){
		return IntStream.range(0, input.length()).mapToObj(input::charAt).allMatch(Character::isDigit);
	}
	boolean test(String input){
		if(input.startsWith("-")){
			return allDigits(input.substring(1));
		}
		return allDigits(input);
	}
	struct NumberFilter new(){
		struct NumberFilter this;
		return this;
	}
}