package com.meti;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {

    public static final Path Target = Paths.get(".", "Index.mgs");
    public static final Path Source = Paths.get(".", "Index.java");

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Source);
        Files.deleteIfExists(Target);
    }

    private static Optional<Path> tryRunWithSource() {
        try {
            Files.createFile(Source);
            return tryRun();
        } catch (IOException e) {
            fail(e);
            return Optional.empty();
        }
    }

    private static Optional<Path> tryRun() {
        try {
            return new Application(new SingleSource(Source), Paths.get(".")).runSingle();
        } catch (IOException e) {
            fail(e);
            return Optional.empty();
        }
    }

    @Test
    void generateTarget() {
        tryRunWithSource();
        assertTrue(Files.exists(Target));
    }

    @Test
    void generatesCorrectTarget() {
        assertEquals(Target, tryRunWithSource().orElseThrow());
    }

    @Test
    void generatesNoTarget() {
        tryRun();
        assertFalse(Files.exists(Target));
    }
}
