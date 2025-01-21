 struct StringContext(String value) implements Context {
	@Override
 String display(){
		return this.value;
	}
}
