package magma;

import magma.compile.MapNode;
import magma.compile.lang.CommonLang;
import magma.core.String_;
import magma.java.JavaString;
import magma.java.io.JavaPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final JavaPath SOURCE = resolveByExtension(JavaPath.EXTENSION_SEPARATOR + "java");
    public static final JavaPath TARGET = resolveByExtension(Application.MAGMA_EXTENSION);

    private static JavaPath resolveByExtension(String extension) {
        return new JavaPath(Paths.get(".").resolve("ApplicationTest" + extension));
    }

    private static void runOrFail() {
        new Application(SOURCE)
                .run()
                .ifPresent(err -> {
                    System.err.println(err.findMessage().unwrap());
                    fail();
                });
    }

    private static void runWithInput(String input) {
        SOURCE.writeSafe(new JavaString(input)).ifPresent(Assertions::fail);
        runOrFail();
    }

    private static void assertRun(String input, String output) {
        runWithInput(input);
        TARGET.readString()
                .mapValue(String_::unwrap)
                .consume(value -> assertEquals(output, value), Assertions::fail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        final var node = new MapNode()
                .withString("namespace", new JavaString(namespace))
                .orElse(new MapNode());

        final var value = CommonLang.createImportRule().generate(node).orElse(JavaString.EMPTY).unwrap();
        assertRun(value, value);
    }

    @Test
    void packageStatement() {
        assertRun("package magma;", "");
    }

    @AfterEach
    void tearDown() {
        TARGET.deleteIfExists()
                .or(SOURCE::deleteIfExists)
                .ifPresent(Assertions::fail);
    }

    @Test
    void generatesTarget() {
        runWithInput("");
        assertTrue(TARGET.exists());
    }

    @Test
    void generatesNoTarget() {
        assertFalse(TARGET.exists());
    }
}