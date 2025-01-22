import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.Optional;struct FirstLocator(String infix) implements Locator{
	String unwrap();
	int length();
	Stream<Integer> locate(String input);
}