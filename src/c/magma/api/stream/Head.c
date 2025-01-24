import java.util.Optional;struct Head<T>{
	struct Table{
		Optional<T> next();
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}