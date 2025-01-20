import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.app.rule.locate.Locator;
import java.util.ArrayList;
import java.util.List;
public struct ForwardsLocator implements Locator {
	private final String infix;
	((String) => public) ForwardsLocator=public ForwardsLocator(String infix){
		this.infix =infix;
	};
	(() => String) unwrap=String unwrap(){
		return this.infix;
	};
	(() => int) length=int length(){
		return this.infix.length();
	};
	((String) => Stream<Integer>) locate=Stream<Integer> locate(String input){
		return Streams.from(searchForIndices(input));
	};
	((String) => List<Integer>) searchForIndices=List<Integer> searchForIndices(String input){
		List<Integer> indices=new ArrayList<>();
		int index=input.indexOf(this.infix);
		while(index>=0){
		indices.add(index);
		index=input.indexOf(this.infix, index+1);
	}
		return indices;
	};
}