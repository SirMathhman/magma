import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.Optional;

String unwrap(){
	return this.infix;
}

int length(){
	return this.infix.length();
}

Stream<Integer> locate(String input){
	const auto index=input.indexOf(this.infix);
	return index==-1?Streams.empty():Streams.of(index);
}
struct FirstLocator(String infix) implements Locator {
}
