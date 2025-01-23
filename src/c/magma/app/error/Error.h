struct Error{
	struct VTable{
		((void*) => String) display;
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