struct Error{
	struct VTable{
		((Any) => String) display;
	}
	struct VTable vtable;
	struct Error new(struct VTable table){
		struct Error this;
		this.table=table;
		return this;
	}
}