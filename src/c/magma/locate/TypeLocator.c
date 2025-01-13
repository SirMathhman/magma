#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct TypeLocator implements Locator {
	char search;
	char enter;
	char exit;
	Tuple<Optional<Integer>, Integer> fold(Tuple<Integer, Character> input, Tuple<Optional<Integer>, Integer> current, LinkedList<Tuple<Integer, Character>> queue){
		auto found = current.left();
		return current;
		auto depth = current.right();
		auto index = input.left();
		auto c = input.right();
		(Optional.of(index), depth);
		(Optional.empty(), depth + 1);
		(Optional.empty(), );
		return temp();
	}
	int computeLength(){
		return 1;
	}
	Optional<Integer> locate(String input){
		auto queue = auto temp(){}(Collectors.toCollection(LinkedList.new));
		(Optional.empty(), 0);
		return state.left();
	}
	String createErrorMessage(){
		return "No space present.";
	}
};