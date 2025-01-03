package magma.app;

import magma.app.compile.CompileException;

import java.util.Optional;

public record StructCompiler(String infix) implements Compiler {
    @Override
    public Optional<String> compile(String input) throws CompileException {
        if (!input.contains(infix)) return Optional.empty();

        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var contentEnd = input.lastIndexOf('}');
        if (contentEnd == -1) return Optional.empty();

        final var content = input.substring(contentStart + 1, contentEnd);
        final var segments = Splitter.split(content);
        var builder = new StringBuilder();
        for (String segment : segments) {
            builder.append(compileClassSegment(segment));
        }

        return Optional.of("struct Temp {" + builder + "};");
    }

    private String compileClassSegment(String classSegment) throws CompileException {
        throw new CompileException("Unknown class segment", classSegment);
    }
}