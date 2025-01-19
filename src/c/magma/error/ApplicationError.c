struct ApplicationError {
	Error cause;
	String display(){
		return this.cause.display();
	}
};