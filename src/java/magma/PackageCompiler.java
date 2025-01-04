package magma;

import java.util.Optional;

public class PackageCompiler implements Compiler {
    @Override
    public Optional<String> compile(String rootSegment) {
        return rootSegment.startsWith("package ") ? Optional.of("") : Optional.empty();
    }
}