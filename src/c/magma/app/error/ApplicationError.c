struct ApplicationError(Error cause){
	String display(){
		return this.cause.display();
	}
	Error N/A(){
		return N/A.new();
	}
}