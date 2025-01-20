import java.util.Optional;
public struct Locator {
	String unwrap();
	int length();
	Optional<Integer> locate(String input);
}