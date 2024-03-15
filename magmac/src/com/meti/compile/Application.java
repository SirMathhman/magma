package com.meti.compile;

import com.meti.collect.result.Err;
import com.meti.collect.result.Result;
import com.meti.collect.result.Results;
import com.meti.collect.stream.Collectors;
import com.meti.collect.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class Application {
    private final SourceSet source;

    public Application(SourceSet source1) {
        this.source = source1;
    }

    public Result<List<Path>, CompileException> run() {
        Set<Path> sources;
        try {
            sources = source.collectSources();
        } catch (IOException e) {
            return new Err<>(new CompileException(e));
        }

        return Streams.fromSet(sources).map(source -> Results.$Result(() -> {
            String input;
            try {
                input = Files.readString(source);
            } catch (IOException e) {
                throw new CompileException(e);
            }
            String output;
            try {
                output = new Compiler(input).compile();
            } catch (CompileException e) {
                throw new CompileException("Failed to compile file: '" + source + "'", e);
            }

            var fileName = source.getFileName().toString();
            var index = fileName.indexOf(".");
            var name = fileName.substring(0, index);
            var target = source.resolveSibling(name + ".mgs");
            try {
                Files.writeString(target, output);
            } catch (IOException e) {
                throw new CompileException(e);
            }
            return target;
        })).collect(Collectors.exceptionally(Collectors.toList()));
    }
}