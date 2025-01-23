import magma.api.stream.Stream;struct Locator{
	struct VTable{
		String (*)(void*) unwrap;
		int (*)(void*) length;
		Stream<Integer> (*)(void*, String) locate;
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