import magma.api.stream.Stream;import magma.api.stream.Streams;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.List;struct BackwardsLocator{
	String infix;
	public BackwardsLocator(any* _ref_, String infix){
		this.infix =infix;
	}
	String unwrap(any* _ref_){
		return this.infix;
	}
	int length(any* _ref_){
		return this.infix.length();
	}
	Stream<Integer> locate(any* _ref_, String input){
		return Streams.from(searchForIndices(input));
	}
	List<Integer> searchForIndices(any* _ref_, String input){
		List<Integer> indices=new ArrayList<>();
		int index=input.lastIndexOf(this.infix);
		while(index>=0){
			indices.add(index);
			index=input.lastIndexOf(this.infix, index - 1);
		}
		return indices;
	}
	Locator N/A(any* _ref_){
		return N/A.new();
	}
}