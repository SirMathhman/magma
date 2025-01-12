package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public record JavaPath(Path path) implements magma.io.Path {
    @Override
    public Result<JavaSet<magma.io.Path>, IOException> walk() {
        try (var stream = Files.walk(unwrap())) {
            return new Ok<>(new JavaSet<>(stream.map(JavaPath::new).collect(Collectors.toSet())));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    @Override
    public Result<String, IOException> readString() {
        try {
            return new Ok<>(Files.readString(unwrap()));
        } catch (IOException e) {
            return new Err<>(e);
        }
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
    public Option<IOException> createDirectories() {
        try {
            Files.createDirectories(unwrap());
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    @Override
    public magma.io.Path relativize(magma.io.Path child) {
        return new JavaPath(this.path.relativize(child.unwrap()));
    }

    @Override
    public Option<magma.io.Path> getParent() {
        final var parent = this.path.getParent();
        return parent == null ? new Some<>(new JavaPath(parent)) : new None<>();
    }

    @Override
    public magma.io.Path resolve(String segment) {
        return new JavaPath(this.path.resolve(segment));
    }

    @Override
    public int getNameCount() {
        return this.path.getNameCount();
    }

    @Override
    public Option<magma.io.Path> getName(int index) {
        if (index < getNameCount()) {
            return new Some<>(new JavaPath(this.path.getName(index)));
        } else {
            return new None<>();
        }
    }

    @Override
    public Path unwrap() {
        return this.path;
    }

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(this.path);
    }

    @Override
    public magma.io.Path getFileName() {
        return new JavaPath(this.path.getFileName());
    }

    @Override
    public boolean exists() {
        return Files.exists(this.path);
    }
}