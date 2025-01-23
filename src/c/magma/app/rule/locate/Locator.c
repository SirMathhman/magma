import magma.api.stream.Stream;struct Locator<Capture>{
	struct VTable{
		(() => String) unwrap;
		(() => int) length;
		((String) => Stream<Integer>) locate;
	}
	struct VTable vtable;
	struct Locator new(struct VTable table){
		struct Locator this;
		this.table=table;
		return this;
	}
}