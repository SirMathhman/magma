package magma.app;

import magma.app.compile.CompileException;

import java.util.Optional;

public interface Compiler {
    Optional<String> compile(String input) throws CompileException;
}
