struct StringContext(String value) implements Context{
	String display(){
		return this.value;
	}struct StringContext new(){struct StringContext this;return this;}
}