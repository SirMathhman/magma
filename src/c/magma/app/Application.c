package magma.app;package magma.app;

import magma.app.io.source.SourceSet;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Application {
    private final TargetSet targetSet;package magma.app;

import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Application {
    private final TargetSet targetSet;
    private final SourceSet sourceSet;package magma.app;

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
        this.sourceSet = sourceSet;package magma.app;

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
        this.targetSet = targetSet;package magma.app;

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
        var segments = new ArrayList<String>();package magma.app;

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
        var buffer = new StringBuilder();package magma.app;

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
        var state = new DividingState(segments, buffer);package magma.app;

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
        for (int i = 0;package magma.app;

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
        for (int i = 0; i < root.length();package magma.app;

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
            var c = root.charAt(i);package magma.app;

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
            state = splitAtChar(state, c);package magma.app;

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

        return state.advance().segments();package magma.app;

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
        final var appended = state.append(c);package magma.app;

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
        if (c == ';package magma.app;

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
        if (c == ';') return appended.advance();package magma.app;

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
        return appended;package magma.app;

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
        if(rootSegment.startsWith("package ")) return rootSegment;package magma.app;

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
        throw new CompileException("Unknown root segment", rootSegment);package magma.app;

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
        final var input = unit.read();package magma.app;

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
        targetSet.write(unit, compile(input));package magma.app;

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
        final var segments = split(root);package magma.app;

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

        var output = new StringBuilder();package magma.app;

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
            output.append(compileRootMember(segment));package magma.app;

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

        return output.toString();package magma.app;

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
        final var sources = sourceSet.collect();package magma.app;

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
            runWithSource(source);package magma.app;

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