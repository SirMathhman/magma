import magma.api.stream.Stream;
public struct Locator {
	(() => String) unwrap;
	(() => int) length;
	((String) => Stream<Integer>) locate;
}