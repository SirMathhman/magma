#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct TypeLocator implements Locator {
	char search;
	char enter;
	char exit;
	public TypeLocator(char search, char enter, char exit){
		this.search = from;
		this.enter = from;
		this.exit = from;
	}
	Tuple<Optional<Integer>, Integer> fold(Tuple<Integer, Character> input, Tuple<Optional<Integer>, Integer> current, LinkedList<Tuple<Integer, Character>> queue){
		auto found = current.left();
		if (found.isPresent() {}
		auto depth = current.right();
		auto index = input.left();
		auto c = input.right();
		if (c == '\'') {}
		if (c == '"') {}
		if (c == this.search && depth == 0) {}
		if (c == this.enter) {}
		if (c == this.exit) {}
		return temp();
	}
	int computeLength(){
		return 1;
	}
	Optional<Integer> locate(String input){
		auto queue = auto temp(){}(Collectors.toCollection(LinkedList.new));
		auto state = temp();
		while (!queue.isEmpty() {}
		return state.left();
	}
};