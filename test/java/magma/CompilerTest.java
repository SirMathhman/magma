package magma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    @Test
    void rootMemberInvalid() {
        assertThrows(CompileException.class, () -> Compiler.compile("?"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"first", "second"})
    void importStatic(String namespace) {
        try {
            final var output = Compiler.compile(Compiler.renderStaticImport(namespace));
            assertEquals(Compiler.renderInstanceImport(namespace), output);
        } catch (CompileException e) {
            fail(e);
        }
    }
}