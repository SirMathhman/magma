package magma.app.compile.rule.locate;

import magma.api.stream.Stream;

public interface Locator {
    String unwrap();

    int length();

    Stream<Integer> locate(String input);
}