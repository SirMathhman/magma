struct State(int depth){
	struct Table{
		boolean isShallow(){
			return depth==1;
		}
	}
	struct Impl{}
}