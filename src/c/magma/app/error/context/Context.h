struct Context{
	struct VTable{
		String (*)(void*) display;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Context new(Box<void*> ref, struct VTable vtable){
		struct Context this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}