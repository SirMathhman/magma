struct CompileError {
	String message;
	String context;
	void new(){
		struct CompileError this;
		this.message = message;
		this.context = context;
		return this;
	}
	void display(void* __ref__){
		struct CompileError* this = (struct CompileError*) __ref__;
		return value;
	}
};