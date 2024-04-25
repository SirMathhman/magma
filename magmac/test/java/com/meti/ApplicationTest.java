package com.meti;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.meti.Compiler.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {
    public static final String TEST_UPPER_SYMBOL = "Test";
    public static final String TEST_LOWER_SYMBOL = "test";

    private static void assertRun(String input, String output) {
        assertEquals(output, run(input));
    }

    static String renderMagmaFunction() {
        return Compiler.renderMagmaFunction(TEST_UPPER_SYMBOL);
    }

    static String renderBeforeClass(String input) {
        return input + renderJavaClass();
    }

    private static String renderJavaClass() {
        return Compiler.renderJavaClass(TEST_UPPER_SYMBOL);
    }

    private static void assertRunWithinClass(String input, String output) {
        assertRun(Compiler.renderJavaClass("", TEST_UPPER_SYMBOL, input),
                Compiler.renderMagmaFunction("", TEST_UPPER_SYMBOL, output));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void definitionName(String name) {
        assertRunWithinClass(renderJavaDefinition(name, INT_KEYWORD, "0"), renderMagmaDefinition(name, I32_KEYWORD, "0"));
    }

    @Test
    void definitionType() {
        assertRunWithinClass(renderJavaDefinition(TEST_LOWER_SYMBOL, LONG_KEYWORD, "0"), renderMagmaDefinition(TEST_LOWER_SYMBOL, I64_KEYWORD, "0"));
    }

    @Test
    void definitionValue() {
        var value = "100";
        assertRunWithinClass(renderJavaDefinition(TEST_LOWER_SYMBOL, LONG_KEYWORD, value),
                renderMagmaDefinition(TEST_LOWER_SYMBOL, I64_KEYWORD, value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void importChildren(String child) {
        assertRun(renderBeforeClass(renderJavaImport(TEST_LOWER_SYMBOL, child)), renderBeforeFunction(renderMagmaImport(TEST_LOWER_SYMBOL, child)));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void importMultiple(int count) {
        var inputBefore = IntStream.range(0, count)
                .mapToObj(index -> renderJavaImport(TEST_LOWER_SYMBOL, TEST_UPPER_SYMBOL + index))
                .collect(Collectors.joining());

        var outputBefore = IntStream.range(0, count)
                .mapToObj(index -> renderMagmaImport(TEST_LOWER_SYMBOL, TEST_UPPER_SYMBOL + index))
                .collect(Collectors.joining());

        assertRun(renderBeforeClass(inputBefore), renderBeforeFunction(outputBefore));
    }

    @Test
    void importStatic() {
        var input = renderBeforeClass(renderJavaImport(TEST_LOWER_SYMBOL, TEST_UPPER_SYMBOL, STATIC_KEYWORD_WITH_SPACE));
        var output = renderBeforeFunction(renderMagmaImport(TEST_LOWER_SYMBOL, TEST_UPPER_SYMBOL));
        assertRun(input, output);
    }

    @Test
    void importParent() {
        var otherParent = "foo";
        assertRun(renderBeforeClass(renderJavaImport(otherParent, TEST_UPPER_SYMBOL)), renderBeforeFunction(renderMagmaImport(otherParent, TEST_UPPER_SYMBOL)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void packageStatement(String name) {
        assertRun(renderBeforeClass("package " + name + STATEMENT_END), renderMagmaFunction());
    }

    @Test
    void classPublic() {
        assertRun(Compiler.renderJavaClass(PUBLIC_KEYWORD_WITH_SPACE, TEST_UPPER_SYMBOL), Compiler.renderMagmaFunction(EXPORT_KEYWORD_WITH_SPACE, TEST_UPPER_SYMBOL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void className(String name) {
        assertRun(Compiler.renderJavaClass(name), Compiler.renderMagmaFunction(name));
    }
}
