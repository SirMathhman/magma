struct State(int depth){
	struct Table{
		public State(){
			this(0);
		}
		State exit(){
			return new State(this.depth - 1);
		}
		State enter(){
			return new State(this.depth + 1);
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}