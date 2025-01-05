struct ApplicationError {
	Error cause;
	void new(){
	}
	void display(void* __ref__){
		struct ApplicationError* this = (struct ApplicationError*) __ref__;
	}
};