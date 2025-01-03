package magma;package magma;

import magma.app.Application;package magma;

import magma.app.Application;
import magma.app.CompileException;package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var sourceSet = new DirectorySourceSet("java", Paths.get(".", "src", "java"));package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var sourceSet = new DirectorySourceSet("java", Paths.get(".", "src", "java"));
            final var targetSet = new DirectoryTargetSet(Paths.get(".", "src", "c"));package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var sourceSet = new DirectorySourceSet("java", Paths.get(".", "src", "java"));
            final var targetSet = new DirectoryTargetSet(Paths.get(".", "src", "c"));
            new Application(sourceSet, targetSet).run();package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var sourceSet = new DirectorySourceSet("java", Paths.get(".", "src", "java"));
            final var targetSet = new DirectoryTargetSet(Paths.get(".", "src", "c"));
            new Application(sourceSet, targetSet).run();
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();package magma;

import magma.app.Application;
import magma.app.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var sourceSet = new DirectorySourceSet("java", Paths.get(".", "src", "java"));
            final var targetSet = new DirectoryTargetSet(Paths.get(".", "src", "c"));
            new Application(sourceSet, targetSet).run();
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
