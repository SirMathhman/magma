struct Error{
	struct VTable{
		String (*)(void*) display;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Error new(Box<void*> ref, struct VTable vtable){
		struct Error this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}