struct ApplicationError(Error cause) implements Error{
	struct Table{
		String display(){
			return this.cause.display();
		}
	}
	struct Impl{}
}