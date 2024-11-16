package magma;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ApplicationTest {
    @Test
    void test() {
        assertIterableEquals(List.of(1), List.of(1));
    }
}
