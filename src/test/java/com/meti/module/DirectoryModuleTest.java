package com.meti.module;

import com.meti.io.Directory;
import com.meti.io.NIOPath;
import com.meti.io.Path;
import com.meti.source.Source;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryModuleTest {
    private static final Path Parent = NIOPath.Root.resolveChild("parent");

    @Test
    void hasSource() throws IOException {
        var parent = Parent.createAsDirectory();

        var child = Parent.resolveChild("child.mg");
        child.createAsFile();

        var module = new DirectoryModule(parent);

        assertTrue(module.hasSource("child", Collections.emptyList()));
    }

    @Test
    void listSources() throws IOException {
        var parent = Parent.createAsDirectory();

        var child = Parent.resolveChild("child.mg");
        child.createAsFile();

        var module = new DirectoryModule(parent);
        assertTrue(module.listSources()
                .stream()
                .map(Source::computeName)
                .allMatch(name -> module.hasSource(name, Collections.emptyList())));
    }

    @Test
    void listSubSources() throws IOException {
        var parent = Parent.createAsDirectory();

        var child = Parent.resolveChild("child");
        child.createAsDirectory();

        var grandChild = child.resolveChild("grandChild.mg");
        grandChild.createAsFile();

        var module = new DirectoryModule(parent);
        assertTrue(module.hasSource("grandChild", List.of("child")));
    }

    @AfterEach
    void tearDown() throws IOException {
        Parent.existingAsDirectory()
                .ifPresent(Directory::deleteAsDirectory);
    }
}