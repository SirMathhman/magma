import magma.api.stream.Stream;struct Locator{
	String unwrap();
	int length();
	Stream<Integer> locate(String input);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}