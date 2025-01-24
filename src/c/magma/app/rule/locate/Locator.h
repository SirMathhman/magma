import magma.api.stream.Stream;struct Locator{
	String unwrap(any* _ref_);
	int length(any* _ref_);
	Stream<Integer> locate(any* _ref_, String input);
	struct Impl{
		struct Impl new(any* _ref_){
			struct Impl this;
			return this;
		}
	}
}