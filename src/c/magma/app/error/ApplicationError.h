struct ApplicationError(Error cause) implements Error{
	String display(){
		return this.cause.display();
	}
	Error N/A(){
		return N/A.new();
	}
}