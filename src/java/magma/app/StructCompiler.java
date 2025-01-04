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
        final var method = compileMethod(classSegment);
        if(method.isPresent()) {
            return method.get();
        }

        throw new CompileException("Unknown class segment", classSegment);
    }

    private Optional<String> compileMethod(String classSegment) throws CompileException {
        final var contentStart = classSegment.indexOf('{');
        if(contentStart == -1) return Optional.empty();

        final var contentEnd = classSegment.lastIndexOf('}');
        if(contentEnd == -1) return Optional.empty();

        final var content = classSegment.substring(contentStart + 1, contentEnd);
        final var segments = Splitter.split(content);
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileUnknownStatement(segment));
        }

        return Optional.of(buffer.toString());
    }

    private static String compileUnknownStatement(String classSegment) throws CompileException {
        throw new CompileException("Unknown class segment", classSegment);
    }
}