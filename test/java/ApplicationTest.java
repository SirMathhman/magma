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
            return evaluateValue(slice);
        }

        return new Err<>(new ApplicationError());
    }

    private static Result<String, ApplicationError> evaluateValue(String slice) {
        if (isNumber(slice)) {
            return new Ok<>(slice);
        } else {
            return new Err<>(new ApplicationError());
        }
    }

    private static boolean isNumber(String slice) {
        for (int i = 0; i < slice.length(); i++) {
            final var c = slice.charAt(i);
            if (!Character.isDigit(c)) return false;
        }

        return true;
    }

    private static void assertRunErr(String input) {
        assertTrue(run(input).isErr());
    }

    @Test
    void returnValueInvalid() {
        assertRunErr("return ?;");
    }

    @Test
    void error() {
        assertRunErr("?");
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
