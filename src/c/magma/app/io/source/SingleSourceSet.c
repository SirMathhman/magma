package magma.app.io.source;package magma.app.io.source;

import magma.app.io.unit.PathUnit;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    @Override
    public Set<Unit> collect() {
        if (Files.exists(source())) {
            return Collections.singleton(new PathUnit(source.getParent(), source));package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    @Override
    public Set<Unit> collect() {
        if (Files.exists(source())) {
            return Collections.singleton(new PathUnit(source.getParent(), source));
        } else {
            return Collections.emptySet();package magma.app.io.source;

import magma.app.io.unit.PathUnit;
import magma.app.io.unit.Unit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

public record SingleSourceSet(Path source) implements SourceSet {
    @Override
    public Set<Unit> collect() {
        if (Files.exists(source())) {
            return Collections.singleton(new PathUnit(source.getParent(), source));
        } else {
            return Collections.emptySet();
        }
    }
}