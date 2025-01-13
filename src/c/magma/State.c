#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct State {
	List<String> segments;
	Deque<Character> queue;
	int depth;
	StringBuilder buffer;
	public State(List<String> segments, StringBuilder buffer, int depth, Deque<Character> queue){
		= segments;
		= buffer;
		= depth;
		= queue;
	}
	public State(Deque<Character> queue){
		this(temp(), temp(), 0, queue);
	}
	Optional<Tuple<State, Character>> pop(){
		if (this.queue.isEmpty()) return Optional.empty();
		return Optional.of(temp());
	}
	boolean isLevel(){
		return this.depth == 0;
	}
	boolean isShallow(){
		return this.depth == 1;
	}
	State exit(){if (this.depth == 0) {
            Results.writeErr("Depth cannot be negative.", "", "");
        }
		this.depth = from;
		return this;
	}
	State append(char c){
		this.buffer.append(c);
		return this;
	}
	State enter(){
		this.depth = from;
		return this;
	}
	State advance(){if (!this.buffer.isEmpty()) {
            this.segments.add(this.buffer.toString());
            this.buffer = new StringBuilder();
        }
		return this;
	}
	Optional<State> appendFromQueue(){
		return appendAndPop().map(Tuple.left);
	}
	Optional<Tuple<State, Character>> appendAndPop(){
		return pop().map(auto temp(){}(State.append));
	}
	Optional<Character> peek(){
		return this.queue.isEmpty() ? Optional.empty() : Optional.of(this.queue.peek());
	}
};