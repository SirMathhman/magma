public struct StringContext(String value) implements Context {
	@Override
public String display(){
		return this.value;
	}
}