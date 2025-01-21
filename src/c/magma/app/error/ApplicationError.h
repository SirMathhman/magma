 struct ApplicationError(Error cause) implements Error {
	@Override
 String display(){
		return this.cause.display();
	}
}

