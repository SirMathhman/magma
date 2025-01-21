import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.Optional;
 struct FirstLocator(String infix) implements Locator {
	@Override
 String unwrap(){
		return this.infix;
	}
	@Override
 int length(){
		return this.infix.length();
	}
	@Override
 Stream<Integer> locate( String input){
		final var index=input.indexOf(this.infix);
		return index==-1?Streams.empty():Streams.of(index);
	}
}
