struct State(int depth) {public State(){
	this(0);}State exit(){
	return new State(this.depth - 1);}State enter(){
	return new State(this.depth + 1);}}