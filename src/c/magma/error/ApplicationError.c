struct ApplicationError {
	Error cause;
	void new(Error cause){
		struct ApplicationError this;
		this.cause = Node[value=cause];
		return this;
	}
	void display(void* __ref__){
		struct ApplicationError this = *(struct ApplicationError*) __ref__;
		return Node[value=Node[value=Node[value=this].cause.display](Node[value=])];
	}
};