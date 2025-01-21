import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.Optional;

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
	 auto index=input.indexOf(this.infix);
	return index==-1?Streams.empty():Streams.of(index);
}
struct FirstLocator(String infix) implements Locator {
}
