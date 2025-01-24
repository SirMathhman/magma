import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.Optional;struct FirstLocator(String infix){
	String unwrap(){
		return this.infix;
	}
	int length(){
		return this.infix.length();
	}
	Stream<Integer> locate(any* _ref_, String input){
		var index=input.indexOf(this.infix);
		return index==-1?Streams.empty():Streams.of(index);
	}
	Locator N/A(){
		return N/A.new();
	}
}