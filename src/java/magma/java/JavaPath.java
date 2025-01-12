package magma.java;

import magma.collect.Set;
import magma.io.Error;
import magma.io.IOError;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.stream.HeadedStream;
import magma.stream.LengthHead;
import magma.stream.Stream;
import magma.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public record JavaPath(Path path) implements magma.io.Path {
    @Override
    public Result<Set<magma.io.Path>, Error> walk() {
        try (var stream = Files.walk(this.path)) {
            return new Ok<>(new JavaSet<>(stream.map(JavaPath::new).collect(Collectors.toSet())));
        } catch (IOException e) {
            return new Err<>(new IOError(new JavaError(e)));
        }
    }

    @Override
    public String toString() {
        return this.path.toString();
    }

    @Override
    public Result<String, Error> readString() {
        try {
            return new Ok<>(Files.readString(this.path));
        } catch (IOException e) {
            return new Err<>(new IOError(new JavaError(e)));
        }
    }

    @Override
    public Option<Error> writeString(String output) {
        try {
            Files.writeString(this.path, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new IOError(new JavaError(e)));
        }
    }

    @Override
    public Option<Error> createDirectories() {
        try {
            Files.createDirectories(this.path);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new IOError(new JavaError(e)));
        }
    }

    @Override
    public magma.io.Path relativize(magma.io.Path child) {
        final var asNativePath = child.streamNames()
                .map(Object::toString)
                .foldLeftWithInit(Paths::get, Path::resolve)
                .orElse(Paths.get("."));

        return new JavaPath(this.path.relativize(asNativePath));
    }

    @Override
    public Option<magma.io.Path> findParent() {
        final var parent = this.path.getParent();
        return parent == null ? new None<>() : new Some<>(new JavaPath(parent));
    }

    @Override
    public magma.io.Path resolve(String segment) {
        return new JavaPath(this.path.resolve(segment));
    }

    private Option<magma.io.Path> getName(int index) {
        return index < this.path.getNameCount()
                ? new Some<>(new JavaPath(this.path.getName(index)))
                : new None<>();
    }

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(this.path);
    }

    @Override
    public magma.io.Path findFileName() {
        return new JavaPath(this.path.getFileName());
    }

    @Override
    public boolean exists() {
        return Files.exists(this.path);
    }

    @Override
    public Stream<magma.io.Path> streamNames() {
        return new HeadedStream<>(new LengthHead(this.path.getNameCount()))
                .map(this::getName)
                .flatMap(Streams::fromOption);
    }
}