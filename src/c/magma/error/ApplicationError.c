struct ApplicationError {
	Error cause;
	struct ApplicationError new(Error cause){
		struct ApplicationError this;
		this.cause = cause;
		return this;
	}
	String display(){
		return this.cause.display();
	}
};