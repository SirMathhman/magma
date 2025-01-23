struct Error{
	struct VTable{
		((Any) => String) display;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Error new(Box<Any> ref, struct VTable vtable){
		struct Error this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}