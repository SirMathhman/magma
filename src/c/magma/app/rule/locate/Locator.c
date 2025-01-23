import magma.api.stream.Stream;struct Locator{
	struct VTable{
		((Any) => String) unwrap;
		((Any) => int) length;
		((Any, String) => Stream<Integer>) locate;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Locator new(Box<Any> ref, struct VTable vtable){
		struct Locator this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}