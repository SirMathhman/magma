public struct State(int depth) {
	(() => boolean) isShallow=boolean isShallow(){
		return depth==1;
	};
}