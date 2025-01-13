#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct State {
	List<String> segments;
	Deque<Character> queue;
	StringBuilder buffer;
	int depth;
	public State(List<String> segments, StringBuilder buffer, int depth, Deque<Character> queue){
		this.segments = from;
		this.buffer = from;
		this.depth = from;
		this.queue = from;
	}
	public State(Deque<Character> queue){
		this(temp(), temp(), 0, queue);
	}
	Optional<Tuple<State, Character>> pop(){
		if (1) {}
		return Optional.of(temp(), this.queue.pop()));
	}
	boolean isLevel(){
		return this.depth == 0;
	}
	boolean isShallow(){
		return this.depth == 1;
	}
	State exit(){
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
	State advance(){
		if (1) {}
		return this;
	}
	Optional<State> appendFromQueue(){
		return appendAndPop().map(Tuple::left);
	}
	Optional<Tuple<State, Character>> appendAndPop(){
		return pop().map(tuple -> tuple.mergeIntoLeft(State::append));
	}
};