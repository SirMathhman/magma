struct Results  {
	void unwrap(void* __ref__){
		struct Results * this = (struct Results *) __ref__;
		destination = source;
		caller();
		destination = source;
		caller();
		caller();
	}
};