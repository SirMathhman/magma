struct ApplicationError {
	Error cause;
	struct ApplicationError new(Error cause){
		struct ApplicationError this;
	}
	String display(){
		return this.cause.display();
	}
};