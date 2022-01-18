package com.meti.app.compile;

import com.meti.api.collect.JavaMap;
import com.meti.app.compile.clang.CFormat;
import com.meti.app.source.Packaging;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CompiledTest {
    static void assertHeaderCompiles(String input, String output) {
        try {
            var package_ = new Packaging("Index", Collections.emptyList());
            var compiler = new CMagmaCompiler(new JavaMap<>(Collections.singletonMap(package_, input)));
            var actual = compiler.compile()
                    .get(package_)
                    .apply(CFormat.Header, "");

            var separator = actual.indexOf("#define");
            var toSplit = actual.indexOf('\n', separator);
            var content = actual.substring(toSplit + 1, actual.length() - "\n#endif\n".length());
            assertEquals(output, content);
        } catch (CompileException e) {
            fail(e);
        }
    }

    public static void assertSourceCompile(String input, String output) {
        try {
            var package_ = new Packaging("Index", Collections.emptyList());
            var compiler = new CMagmaCompiler(new JavaMap<>(Collections.singletonMap(package_, input)));
            var before = compiler.compile()
                    .get(package_)
                    .apply(CFormat.Source, "");
            var separator = before.indexOf('\"', before.indexOf('\"') + 1);
            var after = before.substring(separator + 1).trim();
            assertEquals(output, after);
        } catch (CompileException e) {
            fail(e);
        }
    }
}