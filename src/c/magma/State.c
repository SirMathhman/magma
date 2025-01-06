#include "temp.h";
#include "temp.h";
struct State  {
	List<String> segments;
	StringBuilder buffer;
	int depth;
	void State(void* __ref__){
		struct State * this = (struct State *) __ref__;
		this.segments = segments;
		this.buffer = buffer;
		this.depth = depth;
	}
	void State(void* __ref__){
		struct State * this = (struct State *) __ref__;
		caller();
	}
	void advance(void* __ref__){
		struct State * this = (struct State *) __ref__;
		if (1) {}
		return value;
	}
	void append(void* __ref__){
		struct State * this = (struct State *) __ref__;
		caller();
		return value;
	}
	void isLevel(void* __ref__){
		struct State * this = (struct State *) __ref__;
		return value;
	}
	void enter(void* __ref__){
		struct State * this = (struct State *) __ref__;depth++;
		return value;
	}
	void exit(void* __ref__){
		struct State * this = (struct State *) __ref__;depth--;
		return value;
	}
	void isShallow(void* __ref__){
		struct State * this = (struct State *) __ref__;
		return value;
	}
};