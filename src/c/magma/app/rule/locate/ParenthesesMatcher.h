import magma.api.Tuple;import magma.api.stream.Stream;import magma.api.stream.Streams;import java.util.LinkedList;import java.util.Optional;import java.util.stream.Collectors;import java.util.stream.IntStream;struct ParenthesesMatcher implements Locator{
	String unwrap();
	int length();
	Stream<Integer> locate(String input);}