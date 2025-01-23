struct Context{
	struct VTable{
		(() => String) display;
	}
	struct VTable vtable;
	struct Context new(struct VTable table){
		struct Context this;
		this.table=table;
		return this;
	}
}