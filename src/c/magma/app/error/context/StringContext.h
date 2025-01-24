struct StringContext(String value) implements Context{
	struct Table{
		String display(){
			return this.value;
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}