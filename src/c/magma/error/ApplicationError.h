struct ApplicationError {
	String display(){
		return this.cause.display();
	}
};