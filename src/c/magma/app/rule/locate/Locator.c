import magma.api.stream.Stream;struct Locator{
	struct Table{
		String unwrap();
		int length();
		Stream<Integer> locate(String input);
	}
	struct Impl{}
}