struct ApplicationError {
	Error cause;
	void new(){
		struct ApplicationError this;
		return this;
	}
	void display(void* __ref__){
		struct ApplicationError* this = (struct ApplicationError*) __ref__;
	}
};