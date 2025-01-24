import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.Optional;struct LastLocator(String infix) implements Locator{
	struct Table{
		Stream<Integer> locate(String input){
			var index=input.lastIndexOf(infix());
			return index==-1?Streams.empty():Streams.of(index);
		}
		String unwrap(){
			return this.infix;
		}
		int length(){
			return this.infix.length();
		}
	}
	struct Impl{}
}