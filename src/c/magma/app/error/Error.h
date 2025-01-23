struct Error{
	struct VTable{
		(() => String) display;
	}
	struct Error new(struct VTable table){
		struct Error this;
		return this;
	}
}