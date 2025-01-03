package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;

public final class Application {
    private final TargetSet targetSet;
    private final SourceSet sourceSet;

    public Application(SourceSet sourceSet, TargetSet targetSet) {
        this.sourceSet = sourceSet;
        this.targetSet = targetSet;
    }

    private void runWithSource(Unit unit) throws IOException, CompileException {
        final var input = unit.read();
        targetSet.write(unit, compile(input));
    }

    private String compile(String root) throws CompileException {
        final var segments = split(root);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootMember(segment));
        }

        return output.toString();
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static String compileRootMember(String root) throws CompileException {
        throw new CompileException("Unknown root", root);
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    public void run() throws IOException, CompileException {
        final var sources = sourceSet.collect();
        for (var source : sources) {
            runWithSource(source);
        }
    }
}