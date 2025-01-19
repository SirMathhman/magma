struct StringContext {
	String value;
	struct StringContext StringContext_new(String value){
		struct StringContext this;
		this.value = value;
		return this;
	}
	String StringContext_display(void* _this_){
		struct StringContext this = *(struct StringContext*) this;
		return this.value;
	}
};