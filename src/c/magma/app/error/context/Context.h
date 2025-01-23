struct Context{
	struct VTable{
		((Any) => String) display;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Context new(Box<Any> ref, struct VTable vtable){
		struct Context this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}