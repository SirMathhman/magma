import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {

    public static final String RETURN_KEYWORD_WITH_SPACE = "return ";
    public static final String STATEMENT_END = ";";

    private static String run(String input) {
        if (input.startsWith(RETURN_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return input.substring(RETURN_KEYWORD_WITH_SPACE.length(), input.length() - 1);
        }

        return "";
    }

    @Test
    void nothing() {
        assertTrue(run("").isEmpty());
    }

    @Test
    void returnStatement() {
        assertTrue(run("return;").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void returnValue(String slice) {
        assertEquals(slice, run(RETURN_KEYWORD_WITH_SPACE + slice + STATEMENT_END));
    }
}
