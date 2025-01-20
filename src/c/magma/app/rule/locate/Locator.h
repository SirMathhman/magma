import magma.api.stream.Stream;
public struct Locator {
	String unwrap();
	int length();
	Stream<Integer> locate(String input);
}