package com.meti;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureTest {
    protected static void assertCompile(String input, String expected) {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            try {
                var actual = Compiler.compile(input);
                assertEquals(expected, actual);
            } catch (CompileException e) {
                fail(e);
            }
        });
    }
}
