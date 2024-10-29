package magma.compile;

import magma.compile.lang.CommonLang;
import magma.compile.lang.JavaLang;
import magma.java.JavaString;
import org.junit.jupiter.api.Test;

import static magma.compile.lang.CommonLang.NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class CompilerTest {

    @Test
    void multipleRootMembers() {
        final var packageNode = new MapNode()
                .withString(NAMESPACE, new JavaString("first"))
                .orElse(new MapNode());

        final var importNode = new MapNode()
                .withString(NAMESPACE, new JavaString("second"))
                .orElse(new MapNode());

        final var generatedPackage = JavaLang.createPackageRule()
                .generate(packageNode)
                .orElse(JavaString.EMPTY);

        final var generatedImport = CommonLang.createImportRule()
                .generate(importNode)
                .orElse(JavaString.EMPTY);

        final var input = generatedPackage.appendOwned(generatedImport);

        new Compiler(input).compile().consume(actual -> assertEquals(generatedImport.unwrap(), actual.unwrap()), err -> {
            System.err.println(err.findMessage());
            fail();
        });
    }
}