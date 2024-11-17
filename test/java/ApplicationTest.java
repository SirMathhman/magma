import magma.ApplicationError;
import magma.Err;
import magma.Ok;
import magma.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final String RETURN_KEYWORD_WITH_SPACE = "return";
    public static final String STATEMENT_END = ";";

    private static String runOrFail(String input) {
        return run(input).match(value -> value, _ -> fail());
    }

    private static Result<String, ApplicationError> run(String input) {
        if (input.isEmpty()) return new Ok<>("");

        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            final var slice = input.substring(RETURN_KEYWORD_WITH_SPACE.length(), input.length() - 1).strip();
            return new Ok<>(slice);
        }

        return new Err<>(new ApplicationError());
    }

    @Test
    void error() {
        assertTrue(run("?").isErr());
    }

    @Test
    void nothing() {
        assertTrue(runOrFail("").isEmpty());
    }

    @Test
    void returnStatement() {
        assertTrue(runOrFail("return;").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returnValue(String slice) {
        assertEquals(slice, runOrFail(RETURN_KEYWORD_WITH_SPACE + " " + slice + STATEMENT_END));
    }
}
