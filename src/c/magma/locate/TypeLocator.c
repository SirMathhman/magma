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
		= search;
		= enter;
		= exit;
	}
	Tuple<Optional<Integer>, Integer> fold(Tuple<Integer, Character> input, Tuple<Optional<Integer>, Integer> current, LinkedList<Tuple<Integer, Character>> queue){
		auto found = current.left();
		return current;
		auto depth = current.right();
		auto index = input.left();
		auto c = input.right();if (c == '\'') {
            queue.pop();
            if(!queue.isEmpty() && queue.peek().right() == '\\') {
                queue.pop();
            }

            queue.pop();
        }if (c == '"') {
            while (!queue.isEmpty()) {
                final var next = queue.pop().right();
                if (next == '"') break;

                if (!queue.isEmpty() && queue.peek().right() == '\\') {
                    queue.pop();
                }
            }

            return new Tuple<>(Optional.empty(), depth);
        }
		if (c == this.search && depth == 0) return new Tuple<>(Optional.of(index), depth);
		if (c == this.enter) return new Tuple<>(Optional.empty(), depth + 1);
		if (c == this.exit) return new Tuple<>(Optional.empty(), depth - 1);
		return temp();
	}
	int computeLength(){
		return 1;
	}
	Optional<Integer> locate(String input){
		auto queue = auto temp(){}(Collectors.toCollection(LinkedList.new));
		auto state = temp();while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = fold(c, state, queue);
        }
		return state.left();
	}
	String createErrorMessage(){
		return "No space present.";
	}
};