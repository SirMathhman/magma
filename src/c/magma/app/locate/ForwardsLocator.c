import magma.api.stream.Stream;import magma.api.stream.Streams;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.List;struct ForwardsLocator{
	String infix;
	public ForwardsLocator(any* _ref_, String infix){
		this.infix =infix;
	}
	String unwrap(){
		return this.infix;
	}
	int length(){
		return this.infix.length();
	}
	Stream<Integer> locate(any* _ref_, String input){
		return Streams.from(searchForIndices(input));
	}
	List<Integer> searchForIndices(any* _ref_, String input){
		List<Integer> indices=new ArrayList<>();
		int index=input.indexOf(this.infix);
		while(index>=0){
			indices.add(index);
			index=input.indexOf(this.infix, index+1);
		}
		return indices;
	}
	Locator N/A(){
		return N/A.new();
	}
}