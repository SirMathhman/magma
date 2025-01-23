struct Context{
	struct VTable{
		(() => String) display;
	}
	struct Context new(struct VTable table){
		struct Context this;
		return this;
	}
}