struct ApplicationError {
	Error cause;
	struct ApplicationError ApplicationError_new(Error cause){
		struct ApplicationError this;
		this.cause = cause;
		return this;
	}
	String ApplicationError_display(void* _this_){
		struct ApplicationError this = *(struct ApplicationError*) this;
		return this.cause.display();
	}
};