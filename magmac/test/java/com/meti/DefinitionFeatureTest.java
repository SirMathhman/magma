package com.meti;

import com.meti.java.JavaClassNodeBuilder;
import com.meti.magma.DeclarationNodeBuilder;
import com.meti.magma.MagmaClassNodeBuilder;
import com.meti.magma.MagmaDefinitionBuilder;
import com.meti.magma.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.meti.CompiledTest.assertCompile;
import static com.meti.FeatureTest.TEST_SYMBOL;
import static com.meti.FeatureTest.assertWithinClass;

public class DefinitionFeatureTest {
    public static final String TEST_VALUE = "100";
    public static final DeclarationNodeBuilder DEFAULT_INPUT = new DeclarationNodeBuilder()
            .withValue("0")
            .withName(TEST_SYMBOL)
            .withType(Lang.INT);

    @Test
    void testNoValue() {
        assertWithinClass(DEFAULT_INPUT.withName("first"), new MagmaDefinitionBuilder()
                .withMutability(Lang.LET_KEYWORD)
                .withName("first")
                .withType(Lang.I32)
                .withValue("0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void testSimpleDefinition(String name) {
        assertWithinClass(DEFAULT_INPUT.withName(name), new MagmaDefinitionBuilder()
                .withMutability(Lang.LET_KEYWORD)
                .withName(name)
                .withType(Lang.I32)
                .withValue("0"));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void testMultipleDefinitions(int count) {
        assertWithinClass(DEFAULT_INPUT.build().render().repeat(count), new MagmaDefinitionBuilder()
                .withMutability(Lang.LET_KEYWORD)
                .withName(TEST_SYMBOL)
                .withType(Lang.I32)
                .withValue("0")
                .build()
                .render()
                .repeat(count));
    }

    @Test
    void testType() {
        assertWithinClass(DEFAULT_INPUT.withType(Lang.LONG), new MagmaDefinitionBuilder()
                .withMutability(Lang.LET_KEYWORD)
                .withName(TEST_SYMBOL)
                .withType(Lang.I64)
                .withValue("0"));
    }

    @Test
    void testValue() {
        assertWithinClass(DEFAULT_INPUT.withValue(TEST_VALUE), new MagmaDefinitionBuilder()
                .withMutability(Lang.LET_KEYWORD)
                .withName(TEST_SYMBOL)
                .withType(Lang.I32)
                .withValue(TEST_VALUE));
    }

    @Test
    void testFinal() {
        assertWithinClass(DEFAULT_INPUT.withFlagString(Lang.FINAL_KEYWORD + " ").withValue(TEST_VALUE), new MagmaDefinitionBuilder()
                .withMutability(Lang.CONST_KEYWORD)
                .withName(TEST_SYMBOL)
                .withType(Lang.I32)
                .withValue(TEST_VALUE));
    }

    @ParameterizedTest
    @ValueSource(strings = {TEST_SYMBOL, TEST_SYMBOL + "_more"})
    void testStatic(String className) {
        String content = new MagmaDefinitionBuilder().withMutability(Lang.LET_KEYWORD).withName(TEST_SYMBOL).withType(Lang.I32).withValue(TEST_VALUE).build().render();
        String content1 = DEFAULT_INPUT
                .withValue(TEST_VALUE)
                .withFlagString("static ")
                .build()
                .render();

        assertCompile(new JavaClassNodeBuilder()
                        .withPrefix("")
                        .withName(className)
                        .withContent(content1)
                        .build()
                        .render(),
                new ObjectNode("", className, content).render()
                + "\n\n"
                + new MagmaClassNodeBuilder()
                        .withName(className)
                        .build()
                        .render());
    }

    @Test
    void testPublic() {
        assertWithinClass(DEFAULT_INPUT.withFlagString("public ").withValue(TEST_VALUE), new MagmaDefinitionBuilder()
                .withFlags("pub ")
                .withMutability(Lang.LET_KEYWORD)
                .withName(TEST_SYMBOL)
                .withType(Lang.I32)
                .withValue(TEST_VALUE));
    }
}
