struct ApplicationError(Error cause) implements Error{
	String display(){
		return this.cause.display();
	}struct ApplicationError(Error cause) implements Error new(){struct ApplicationError(Error cause) implements Error this;return this;}
}