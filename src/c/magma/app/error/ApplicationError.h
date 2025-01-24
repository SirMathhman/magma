struct ApplicationError(Error cause){
	String display(any* _ref_){
		return this.cause.display();
	}
	Error N/A(any* _ref_){
		return N/A.new();
	}
}