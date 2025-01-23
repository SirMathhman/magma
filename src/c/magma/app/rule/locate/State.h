struct State(int depth){
	boolean isShallow(){
		return depth==1;
	}
	struct State new(){
		struct State this;
		return this;
	}
}