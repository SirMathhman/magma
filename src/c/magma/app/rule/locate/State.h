struct State(int depth){
	struct Table{
		boolean isShallow(){
			return depth==1;
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}