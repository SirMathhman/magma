struct Error{
	String display();
	struct Error new(){
		struct Error this;
		return this;
	}
}