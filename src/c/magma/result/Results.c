struct Results  {
	void unwrap(void* __ref__){
		struct Results * this = (struct Results *) __ref__;
		final var maybeValue = result.findValue();
		caller();
		final var maybeError = result.findError();
		caller();
		caller();
	}
};