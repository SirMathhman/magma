package magma.app;

import magma.app.compile.CompileException;
import magma.app.compile.DividingState;
import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Application {
    private final TargetSet targetSet;
    private final SourceSet sourceSet;

    public Application(SourceSet sourceSet, TargetSet targetSet) {
        this.sourceSet = sourceSet;
        this.targetSet = targetSet;
    }

    private static List<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var state = new DividingState(segments, buffer);
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments();
    }

    private static DividingState splitAtChar(DividingState state, char c) {
        final var appended = state.append(c);
        if (c == ';') return appended.advance();
        return appended;
    }

    private static String compileRootMember(String rootSegment) throws CompileException {
        if(rootSegment.startsWith("package ")) return rootSegment;
        throw new CompileException("Unknown root segment", rootSegment);
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

    public void run() throws IOException, CompileException {
        final var sources = sourceSet.collect();
        for (var source : sources) {
            runWithSource(source);
        }
    }
}