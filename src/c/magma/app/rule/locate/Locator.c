import magma.api.stream.Stream;struct Locator{
	String unwrap();
	int length();
	Stream<Integer> locate(String input);
	struct Locator new(){
		struct Locator this;
		return this;
	}
}