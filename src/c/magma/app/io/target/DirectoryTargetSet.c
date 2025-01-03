package magma.app.io.target;package magma.app.io.target;

import magma.app.io.unit.Unit;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);
        Files.writeString(parent.resolve(name + ".c"), output);package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);
        Files.writeString(parent.resolve(name + ".c"), output);
    }

    private Path resolveParent(List<String> namespace) {
        var current = root;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);
        Files.writeString(parent.resolve(name + ".c"), output);
    }

    private Path resolveParent(List<String> namespace) {
        var current = root;
        for (var segment : namespace) {
            current = current.resolve(segment);package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);
        Files.writeString(parent.resolve(name + ".c"), output);
    }

    private Path resolveParent(List<String> namespace) {
        var current = root;
        for (var segment : namespace) {
            current = current.resolve(segment);
        }
        return current;package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);
        Files.writeString(parent.resolve(name + ".c"), output);
    }

    private Path resolveParent(List<String> namespace) {
        var current = root;
        for (var segment : namespace) {
            current = current.resolve(segment);
        }
        return current;
    }
}