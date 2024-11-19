package magma;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static magma.Compiler.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path SOURCE = resolve("java");
    public static final Path TARGET = resolve("mgs");

    private static Path resolve(String extension) {
        return Paths.get(".", "ApplicationTest." + extension);
    }

    private static void run() throws IOException, CompileException {
        if (!Files.exists(SOURCE)) return;

        final var input = Files.readString(SOURCE);
        final var output = compile(input);
        Files.writeString(TARGET, output);
    }

    private static void assertRun(String input, String output) {
        try {
            Files.writeString(SOURCE, input);
            runOrFail();
            assertEquals(output, Files.readString(TARGET));
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void runOrFail() {
        try {
            run();
        } catch (IOException | CompileException e) {
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageAndImport(String namespace) {
        final var packageNode = new Node(PACKAGE_TYPE).withString(VALUE, namespace);
        final var importNode = new Node(IMPORT_TYPE).withString(VALUE, namespace);

        final var renderedPackage = createPackageRule().generate(packageNode).orElseThrow();
        final var renderedImport = createInstanceImportRule().generate(importNode).orElseThrow();
        assertRun(renderedPackage + renderedImport, renderedImport);
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String namespace) {
        final var packageNode = new Node(PACKAGE_TYPE).withString(VALUE, namespace);
        final var renderedPackage = createPackageRule().generate(packageNode);
        assertRun(renderedPackage.orElse(""), "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatement(String namespace) {
        var sourceNode = new Node(IMPORT_TYPE).withString(VALUE, namespace);

        final var renderedNode = createInstanceImportRule().generate(sourceNode);
        assertRun(renderedNode.orElseThrow(), renderedNode.orElseThrow());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TARGET);
        Files.deleteIfExists(SOURCE);
    }

    @Test
    void generatesNoTarget() {
        runOrFail();
        assertFalse(Files.exists(TARGET));
    }

    @Test
    void generatesTarget() throws IOException {
        Files.createFile(SOURCE);
        runOrFail();
        assertTrue(Files.exists(TARGET));
    }
}
