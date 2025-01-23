import magma.api.stream.Stream;struct Locator{
	struct VTable{
		((void*) => String) unwrap;
		((void*) => int) length;
		((void*, String) => Stream<Integer>) locate;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Locator new(Box<void*> ref, struct VTable vtable){
		struct Locator this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}