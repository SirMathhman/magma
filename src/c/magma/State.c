#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct State {
	List<String> segments;
	Deque<Character> queue;
	int depth;
	StringBuilder buffer;
	Optional<Character> peek(){
		return this.queue.isEmpty() ? Optional.empty() : Optional.of(this.queue.peek());
	}
};