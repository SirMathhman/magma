struct StringContext(String value) implements Context{
	String display(){
		return this.value;
	}
	Context N/A(){
		return N/A.new();
	}
}