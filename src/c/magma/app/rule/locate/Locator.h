import magma.api.stream.Stream;struct Locator{
	struct VTable{
		(() => String) unwrap;
		(() => int) length;
		((String) => Stream<Integer>) locate;
	}
	struct Locator new(){
		struct Locator this;
		return this;
	}
}