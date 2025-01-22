import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.Optional;struct LastLocator(String infix) implements Locator{
	Stream<Integer> locate(String input);
	String unwrap();
	int length();}