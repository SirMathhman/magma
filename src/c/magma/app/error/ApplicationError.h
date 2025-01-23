struct ApplicationError(Error cause) implements Error{
	String display(){
		return this.cause.display();
	}struct ApplicationError new(){struct ApplicationError this;return this;}
}