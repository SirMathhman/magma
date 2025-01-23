import magma.api.stream.Stream;struct Locator{
	struct VTable{
		((Any) => String) unwrap;
		((Any) => int) length;
		((Any, String) => Stream<Integer>) locate;
	}
	struct VTable vtable;
	struct Locator new(struct VTable table){
		struct Locator this;
		this.table=table;
		return this;
	}
}