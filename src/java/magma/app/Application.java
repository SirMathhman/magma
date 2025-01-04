package magma.app;

import magma.app.compile.CompileException;
import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.List;

public final class Application {
    private final TargetSet targetSet;
    private final SourceSet sourceSet;

    public Application(SourceSet sourceSet, TargetSet targetSet) {
        this.sourceSet = sourceSet;
        this.targetSet = targetSet;
    }

    private static String compileRootMember(String rootSegment) throws CompileException {
        final var compilers = List.of(
                new PackageCompiler(),
                new ImportCompiler(),
                new StructCompiler("interface"),
                new StructCompiler("class"),
                new StructCompiler("record")
        );

        for (Compiler compiler : compilers) {
            final var compiled = compiler.compile(rootSegment);
            if (compiled.isPresent()) {
                return compiled.get();
            }
        }

        throw new CompileException("Unknown root segment", rootSegment);
    }

    private void runWithSource(Unit unit) throws IOException, CompileException {
        final var input = unit.read();
        targetSet.write(unit, compile(input));
    }

    private String compile(String root) throws CompileException {
        final var segments = Splitter.split(root);

        var output = new StringBuilder();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (!stripped.isEmpty()) {
                final var compiled = compileRootMember(stripped);
                output.append(compiled);
            }
        }

        return output.toString();
    }

    public void run() throws IOException, CompileException {
        final var sources = sourceSet.collect();
        for (var source : sources) {
            System.out.println(source);
            runWithSource(source);
        }
    }
}