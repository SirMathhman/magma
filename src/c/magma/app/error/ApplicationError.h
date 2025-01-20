public struct ApplicationError(Error cause) implements Error {
	(() => String) display=String display(){
		return this.cause.display();
	};
}