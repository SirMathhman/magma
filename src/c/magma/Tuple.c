struct Tuple<A, B> {
	A left;
	B right;
	struct Tuple<A, B> new(A left, B right){
		struct Tuple<A, B> this;
		this.left = left;
		this.right = right;
		return this;
	}
};