struct CompileError {
	String message;
	String context;
	void new(){
		struct CompileError this;
		return this;
	}
	void display(void* __ref__){
		struct CompileError* this = (struct CompileError*) __ref__;
	}
};