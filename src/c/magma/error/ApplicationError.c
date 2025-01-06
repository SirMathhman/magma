struct ApplicationError {
	Error cause;
	void new(Error cause){
		struct ApplicationError this;
		this.cause = cause;
		return this;
	}
	void display(void* __ref__){
		struct ApplicationError this = *(struct ApplicationError*) __ref__;
		return {
			void __caller__ = this.cause.display;
			__caller__.()
		};
	}
};