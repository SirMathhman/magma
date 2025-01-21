import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.Optional;
struct LastLocator(String infix) implements Locator {
	@Override
Stream<Integer> locate(String input){
		 auto index=input.lastIndexOf(infix());
		return index==-1?Streams.empty():Streams.of(index);
	}
	@Override
String unwrap(){
		return this.infix;
	}
	@Override
int length(){
		return this.infix.length();
	}
}
