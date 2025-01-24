import magma.api.stream.Stream;import magma.api.stream.Streams;import magma.app.rule.locate.Locator;import java.util.Optional;struct InvocationLocator{
	String unwrap(any* _ref_){
		return "(";
	}
	int length(any* _ref_){
		return 1;
	}
	Stream<Integer> locate(any* _ref_, String input){
		var depth=0;
		int i=input.length() - 1;
		while(i>=0){
			var c=input.charAt(i);
			if(c=='('&&depth==0)return Streams.of(i);
			if(c==')')depth++;
			if(c=='(')depth--;
			i--;
		}
		return Streams.empty();
	}
	Locator N/A(any* _ref_){
		return N/A.new();
	}
}