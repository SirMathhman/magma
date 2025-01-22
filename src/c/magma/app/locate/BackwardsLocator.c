import magma.api.stream.Stream;import magma.api.stream.Streams;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.List;struct BackwardsLocator implements Locator{
	String infix;
	public BackwardsLocator(String infix);
	String unwrap();
	int length();
	Stream<Integer> locate(String input);
	List<Integer> searchForIndices(String input);}