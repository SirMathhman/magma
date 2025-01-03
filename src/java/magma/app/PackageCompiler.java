package magma.app;

import java.util.Optional;

public class PackageCompiler implements Compiler {
    @Override
    public Optional<String> compile(String rootSegment) {
        if (rootSegment.startsWith("package ")) return Optional.of("");
        else return Optional.empty();
    }
}