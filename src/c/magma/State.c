#include "java/util/ArrayList.h";
#include "java/util/List.h";
struct State  {
	List<String> segments;
	StringBuilder buffer;
	int depth;
	void State(void* __ref__, List<String> segments, StringBuilder buffer, int depth){
		struct State  this = *(struct State *) __ref__;
		this.segments = segments;
		this.buffer = buffer;
		this.depth = depth;
	}
	void State(void* __ref__){
		struct State  this = *(struct State *) __ref__;
		caller();
	}
	void advance(void* __ref__){
		struct State  this = *(struct State *) __ref__;
		if (1) {}
		return this;
	}
	void append(void* __ref__, char c){
		struct State  this = *(struct State *) __ref__;
		caller();
		return this;
	}
	void isLevel(void* __ref__){
		struct State  this = *(struct State *) __ref__;
	== 0;
	}
	void enter(void* __ref__){
		struct State  this = *(struct State *) __ref__;depth++;
		return this;
	}
	void exit(void* __ref__){
		struct State  this = *(struct State *) __ref__;depth--;
		return this;
	}
	void isShallow(void* __ref__){
		struct State  this = *(struct State *) __ref__;
	== 1;
	}
};