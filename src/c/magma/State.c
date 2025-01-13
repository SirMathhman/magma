#include "temp.h"
#include "temp.h"
struct State {
	int segments;
	int buffer;
	int depth;
	void State(){
		to = from;
		to = from;
		to = from;
	}
	void State(){
		temp();
	}
	void isLevel(){
		to = from;
	}
	void isShallow(){
		to = from;
	}
	void exit(){
		to = from;
		return temp;
	}
	void append(){
		temp();
		return temp;
	}
	void enter(){
		to = from;
		return temp;
	}
	void advance(){
		to = from;
		return temp;
	}
};