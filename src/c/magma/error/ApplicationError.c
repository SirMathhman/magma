struct ApplicationError {
	Error cause;
	struct ApplicationError new(Error cause){
		struct ApplicationError this;
		this.cause = cause;
		return this;
	}
	String display(void* _this_){
		struct ApplicationError this = *(struct ApplicationError*) this;
		return this.cause.display();
	}
};