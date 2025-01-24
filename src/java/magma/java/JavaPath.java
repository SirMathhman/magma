package magma.java;

import magma.api.io.Path;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.HeadedStream;
import magma.api.stream.RangeHead;
import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public record JavaPath(java.nio.file.Path path) implements Path {
    @Override
    public java.nio.file.Path unwrap() {
        return this.path;
    }

    @Override
    public Path relativize(Path child) {
        return new JavaPath(this.path.relativize(child.unwrap()));
    }

    @Override
    public Option<Path> findParent() {
        final var parent = this.path.getParent();
        return parent == null
                ? new None<>()
                : new Some<>(new JavaPath(parent));
    }

    @Override
    public int getNameCount() {
        return this.path.getNameCount();
    }

    @Override
    public Option<Path> getName(int index) {
        if (index < this.path.getNameCount()) {
            return new Some<>(new JavaPath(this.path.getName(index)));
        }

        return new None<>();
    }

    @Override
    public String format() {
        return this.path.toString();
    }

    @Override
    public Path getFileName() {
        return new JavaPath(this.path.getFileName());
    }

    @Override
    public Path resolvePath(Path child) {
        return new JavaPath(this.path.resolve(child.unwrap()));
    }

    @Override
    public Path resolveChild(String child) {
        return new JavaPath(this.path.resolve(child));
    }

    @Override
    public Result<JavaSet<Path>, IOException> walkWrapped() {
        try (var stream = Files.walk(unwrap())) {
            return new Ok<>(new JavaSet<>(stream
                    .map(JavaPath::new)
                    .collect(Collectors.toSet())));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    @Override
    public boolean isExists() {
        return Files.exists(unwrap());
    }

    @Override
    public Stream<Path> stream() {
        return new HeadedStream<>(new RangeHead(getNameCount()))
                .map(this::getName)
                .flatMap(Streams::fromOption);
    }

    @Override
    public Option<IOException> writeString(String output) {
        try {
            Files.writeString(unwrap(), output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    @Override
    public Option<IOException> createAsDirectories() {
        try {
            Files.createDirectories(unwrap());
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    @Override
    public Result<String, IOException> readStrings() {
        try {
            return new Ok<>(Files.readString(unwrap()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
