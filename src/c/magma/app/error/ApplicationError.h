struct ApplicationError(Error cause) implements Error{
	String display(){
		return this.cause.display();
	}
}
