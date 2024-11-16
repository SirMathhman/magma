package magma;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ApplicationTest {
    @Test
    void test() {
        assertIterableEquals(Collections.emptyList(), Collections.emptyList());
    }
}
