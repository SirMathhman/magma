import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.Optional;struct FirstLocator(String infix) implements Locator{
	struct Table{
		String unwrap(){
			return this.infix;
		}
		int length(){
			return this.infix.length();
		}
		Stream<Integer> locate(String input){
			var index=input.indexOf(this.infix);
			return index==-1?Streams.empty():Streams.of(index);
		}
	}
	struct Impl{}
	struct Table table;
	struct Impl impl;
}