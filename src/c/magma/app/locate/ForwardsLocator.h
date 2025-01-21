import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.app.rule.locate.Locator;
import java.util.ArrayList;
import java.util.List;

public ForwardsLocator(String infix){
	this.infix =infix;
}

@Override
String unwrap(){
	return this.infix;
}

@Override
int length(){
	return this.infix.length();
}

@Override
Stream<Integer> locate(String input){
	return Streams.from(searchForIndices(input));
}

List<Integer> searchForIndices(String input){
	List<Integer> indices=new ArrayList<>();
	int index=input.indexOf(this.infix);
	while(index>=0){
		indices.add(index);
		index=input.indexOf(this.infix, index+1);
	}
	return indices;
}
struct ForwardsLocator implements Locator { String infix;
}

