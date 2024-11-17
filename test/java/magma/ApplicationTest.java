package magma;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplicationTest {
    private static boolean hasNoMemory(Option<Integer> tSome) {
        return tSome.isEmpty();
    }

    @Test
    void simple() {
        assertFalse(hasNoMemory(new Some<>(1)));
    }

    @Test
    void nothing() {
        assertTrue(hasNoMemory(new None<>()));
    }
}
