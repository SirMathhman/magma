import magma.api.Tuple;import magma.api.option.Option;struct Result<T, X>{
	<R>Result<R, X> flatMapValue(any* _ref_, Tuple<any*, Result<R, X> (*)(any*, T)> mapper);
	<R>Result<R, X> mapValue(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper);
	<R>Result<T, R> mapErr(any* _ref_, Tuple<any*, R (*)(any*, X)> mapper);
	<R>R match(any* _ref_, Tuple<any*, R (*)(any*, T)> onOk, Tuple<any*, R (*)(any*, X)> onErr);
	<R>Result<Tuple<T, R>, X> and(any* _ref_, Tuple<any*, Result<R, X> (*)(any*)> other);
	<R>Result<T, Tuple<X, R>> or(any* _ref_, Tuple<any*, Result<T, R> (*)(any*)> other);
	boolean isOk();
	Option<X> findError();
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}