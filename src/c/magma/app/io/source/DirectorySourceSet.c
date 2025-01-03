package magma.app.io.source;package magma.app.io.source;

import magma.app.io.unit.PathUnit;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record DirectorySourceSet(String extension, Path root) implements SourceSet {
    @Override
    public Set<Unit> collect() throws IOException {
        try (var stream = Files.walk(root)) {
            final var files = stream.toList();package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record DirectorySourceSet(String extension, Path root) implements SourceSet {
    @Override
    public Set<Unit> collect() throws IOException {
        try (var stream = Files.walk(root)) {
            final var files = stream.toList();
            
            var sources = new HashSet<Unit>();package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record DirectorySourceSet(String extension, Path root) implements SourceSet {
    @Override
    public Set<Unit> collect() throws IOException {
        try (var stream = Files.walk(root)) {
            final var files = stream.toList();
            
            var sources = new HashSet<Unit>();
            for (var file : files) {
                if (Files.isRegularFile(file) && file.toString().endsWith("." + extension)) {
                    sources.add(new PathUnit(root, file));package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record DirectorySourceSet(String extension, Path root) implements SourceSet {
    @Override
    public Set<Unit> collect() throws IOException {
        try (var stream = Files.walk(root)) {
            final var files = stream.toList();
            
            var sources = new HashSet<Unit>();
            for (var file : files) {
                if (Files.isRegularFile(file) && file.toString().endsWith("." + extension)) {
                    sources.add(new PathUnit(root, file));
                }
            }
            return sources;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public record DirectorySourceSet(String extension, Path root) implements SourceSet {
    @Override
    public Set<Unit> collect() throws IOException {
        try (var stream = Files.walk(root)) {
            final var files = stream.toList();
            
            var sources = new HashSet<Unit>();
            for (var file : files) {
                if (Files.isRegularFile(file) && file.toString().endsWith("." + extension)) {
                    sources.add(new PathUnit(root, file));
                }
            }
            return sources;
        }
    }
}
