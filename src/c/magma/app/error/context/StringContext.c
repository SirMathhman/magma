struct StringContext(String value) implements Context{
	String display(){
		return this.value;
	}
}