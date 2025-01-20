import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.app.rule.locate.Locator;
import java.util.Optional;
public struct InvocationLocator implements Locator {
	(() => String) unwrap=String unwrap(){
		return "(";
	};
	(() => int) length=int length(){
		return 1;
	};
	((String) => Stream<Integer>) locate=Stream<Integer> locate(String input){
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
	};
}