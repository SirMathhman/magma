package magma.app;

import magma.app.compile.CompileException;

import java.util.Optional;

public record StructCompiler(String infix) implements Compiler {
    private static String compileStatement(String statement) throws CompileException {
        if (statement.startsWith("if ")) return "if (1) {}";
        if (statement.contains("=")) return "int value = 0;";
        if (statement.startsWith("return ")) return "return 0;";
        if (statement.contains("(") && statement.endsWith(");")) return "temp();";

        throw new CompileException("Unknown statement", statement);
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
        try {
            final var method = compileMethod(classSegment);
            if (method.isPresent()) return method.get();

            final var definition = compileDefinition(classSegment);
            if (definition.isPresent()) return definition.get();

            throw new CompileException("Unknown class segment", classSegment);
        } catch (CompileException e) {
            throw new CompileException("Invalid class segment", classSegment, e);
        }
    }

    private Optional<String> compileDefinition(String classSegment) {
        return Optional.of("int value = 0;");
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
            final var stripped = segment.strip();
            if (!stripped.isEmpty()) {
                buffer.append(compileStatement(stripped));
            }
        }

        return Optional.of(buffer.toString());
    }
}