
@Override
String display(){
	return this.cause.display();
}
struct ApplicationError(Error cause) implements Error {
}

