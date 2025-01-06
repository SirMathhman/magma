struct Results  {
	void unwrap(void* __ref__, Result<T, X> result){
		struct Results  this = *(struct Results *) __ref__;
		final var maybeValue = {
			void __caller__ = result.findValue;
			__caller__(__caller__)
		};
		caller();
		final var maybeError = {
			void __caller__ = result.findError;
			__caller__(__caller__)
		};
		caller();
		caller();
	}
};