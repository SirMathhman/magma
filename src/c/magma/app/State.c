struct State(int depth){
	public State(){
		this(0);
	}
	State exit(){
		return State.new();
	}
	State enter(){
		return State.new();
	}struct State(int depth) new(){struct State(int depth) this;return this;}
}