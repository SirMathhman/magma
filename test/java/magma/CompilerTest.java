package magma;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CompilerTest {
    @Test
    void rootMemberInvalid() {
        assertThrows(CompileException.class, () -> Compiler.compile("?"));
    }
}