import magma.option.Option;
struct Streams {
	Stream<T> fromOption(Option<T> option){
		return HeadedStream<>(option.<Head<T>>map(SingleHead.new).orElseGet(EmptyHead.new));
	}
}