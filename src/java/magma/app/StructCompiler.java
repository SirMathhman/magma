package magma.app;

import magma.app.compile.CompileException;

import java.util.Optional;

public record StructCompiler(String infix) implements Compiler {
    private static String compileStatement(String classSegment) throws CompileException {
        if (classSegment.startsWith("if ")) return "if (true) {}";
        if (classSegment.contains("=")) return "int value = 0;";

        throw new CompileException("Unknown class segment", classSegment);
    }

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
        final var method = compileMethod(classSegment);
        if (method.isPresent()) {
            return method.get();
        }

        throw new CompileException("Unknown class segment", classSegment);
    }

    private Optional<String> compileMethod(String classSegment) throws CompileException {
        final var contentStart = classSegment.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var contentEnd = classSegment.lastIndexOf('}');
        if (contentEnd == -1) return Optional.empty();

        final var content = classSegment.substring(contentStart + 1, contentEnd);
        final var segments = Splitter.split(content);
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileStatement(segment.strip()));
        }

        return Optional.of(buffer.toString());
    }
}