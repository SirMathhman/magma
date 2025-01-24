struct ApplicationError(Error cause) implements Error{
	struct Table{
		String display(){
			return this.cause.display();
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}