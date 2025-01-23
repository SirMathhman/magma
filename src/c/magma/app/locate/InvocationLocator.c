import magma.api.stream.Stream;import magma.api.stream.Streams;import magma.app.rule.locate.Locator;import java.util.Optional;struct InvocationLocator implements Locator{
	String unwrap(){
		return "(";
	}
	int length(){
		return 1;
	}
	Stream<Integer> locate(String input){
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
	struct InvocationLocator new(){
		struct InvocationLocator this;
		return this;
	}
}