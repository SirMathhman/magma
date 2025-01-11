import magma.option.Option;
struct Streams {
	Stream<T> from(Option<T> option){
		return HeadedStream<>(option.<Head<T>>map(ArrayHead.new).orElseGet(EmptyHead.new));
	}
}