struct State(int depth){
	public State(){
		this(0);
	}
	State exit(){
		return State.new();
	}
	State enter(){
		return State.new();
	}
}