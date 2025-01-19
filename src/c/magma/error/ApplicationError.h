struct ApplicationError {
	Error cause;
	struct ApplicationError new(Error cause){
	}
	String display(){
		return this.cause.display();
	}
};