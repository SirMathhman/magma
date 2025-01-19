struct Error {
	struct Error Error_new(){
		struct Error this;
		return this;
	}
	String Error_display(void* _this_);
};