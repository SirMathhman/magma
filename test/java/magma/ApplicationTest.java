package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    @Test
    void empty() {
        assertEquals("", run(""));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"100", "200"})
    void numeric(String value) {
        assertEquals(value, run(value));
    }

    private static String run(String value) {
        return value;
    }
}
