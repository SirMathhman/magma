struct ApplicationError(Error cause) implements Error{
String display();
}