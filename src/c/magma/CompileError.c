struct CompileError {
	String message;
	String context;
	void new(){
	}
	void display(void* __ref__){
		struct CompileError* this = (struct CompileError*) __ref__;
	}
};