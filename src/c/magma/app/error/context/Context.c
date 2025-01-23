struct Context{
	struct VTable{
		(() => String) display;
	}
	struct Context new(){
		struct Context this;
		return this;
	}
}