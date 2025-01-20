import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.Optional;
public struct LastLocator(String infix) implements Locator {
	((String) => Stream<Integer>) locate=Stream<Integer> locate(String input){
		final var index=input.lastIndexOf(infix());
		return index==-1?Streams.empty():Streams.of(index);
	};
	(() => String) unwrap=String unwrap(){
		return this.infix;
	};
	(() => int) length=int length(){
		return this.infix.length();
	};
}