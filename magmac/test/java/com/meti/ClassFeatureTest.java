package com.meti;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.meti.CompiledTest.assertCompile;

public class ClassFeatureTest {
    public static final String TEST_BODY = "0";

    @ParameterizedTest
    @ValueSource(strings = {"First", "Second"})
    void testSimpleClasses(String name) {
        assertCompile(JavaLang.renderJavaClass(name, ""), Compiler.renderMagmaClass(name));
    }

    @Test
    void testPublicKeyword() {
        assertCompile(Compiler.renderJavaClass(Compiler.PUBLIC_KEYWORD, FeatureTest.TEST_NAME, ""), Compiler.renderExportedMagmaClass(FeatureTest.TEST_NAME));
    }

    @Test
    void testBody() {
        assertCompile(JavaLang.renderJavaClass(FeatureTest.TEST_NAME, TEST_BODY),
                Compiler.renderMagmaClass(FeatureTest.TEST_NAME, TEST_BODY));
    }
}
