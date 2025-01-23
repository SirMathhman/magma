struct Error{
	struct VTable{
		(() => String) display;
	}
	struct Error new(){
		struct Error this;
		return this;
	}
}