package com.meti;

import com.meti.source.SingleSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    public static final Path Source = Paths.get(".", "index.mgs");
    public static final Path TargetHeader = Paths.get(".", "index.h");
    public static final Path TargetSource = Paths.get(".", "index.c");

    @Test
    void native_import() throws IOException, ApplicationException {
        Files.writeString(Source, "import native stdio;");
        new Application(new SingleSource(new PathScript(Source))).run();
        var content = Files.readString(TargetHeader);
        assertEquals("#ifndef index_h\n" +
                "#define index_h\n" +
                "#include <stdio.h>\n" +
                "struct _index_ {}" +
                "struct _index_ __index__();" +
                "#endif\n", content);
    }

    @Test
    void another_native_import() throws IOException, ApplicationException {
        Files.writeString(Source, "import native test;");
        new Application(new SingleSource(new PathScript(Source))).run();
        var content = Files.readString(TargetHeader);
        assertEquals("#ifndef index_h\n" +
                "#define index_h\n" +
                "#include <test.h>\n" +
                "struct _index_ {}" +
                "struct _index_ __index__();" +
                "#endif\n", content);
    }

    @Test
    void multiple_native_imports() throws ApplicationException, IOException {
        Files.writeString(Source, "import native first;import native second;");
        new Application(new SingleSource(new PathScript(Source))).run();
        var content = Files.readString(TargetHeader);
        assertEquals("#ifndef index_h\n" +
                "#define index_h\n" +
                "#include <first.h>\n" +
                "#include <second.h>\n" +
                "struct _index_ {}" +
                "struct _index_ __index__();" +
                "#endif\n", content);
    }

    @Test
    void target_header_content() throws IOException {
        runImpl();

        var content = Files.readString(TargetHeader);
        assertEquals("#ifndef index_h\n" +
                "#define index_h\n" +
                "struct _index_ {}" +
                "struct _index_ __index__();" +
                "#endif\n", content);
    }

    @Test
    void target_source_content() throws IOException {
        runImpl();

        var content = Files.readString(TargetSource);
        assertEquals("struct _index_ __index__(){" +
                "struct _index_ this={};" +
                "return this;" +
                "}", content);
    }

    @Test
    void target_header_present() throws IOException {
        runImpl();
        assertTrue(Files.exists(TargetHeader));
    }

    private void runImpl() throws IOException {
        try {
            Files.createFile(Source);
            new Application(new SingleSource(new PathScript(Source))).run();
        } catch (ApplicationException e) {
            fail(e);
        }
    }

    @Test
    void target_source_present() throws IOException {
        runImpl();
        assertTrue(Files.exists(TargetSource));
    }

    @Test
    void target_header_missing() throws ApplicationException {
        new Application(new SingleSource(new PathScript(Source))).run();
        assertFalse(Files.exists(TargetHeader));
    }

    @Test
    void target_source_missing() throws ApplicationException {
        new Application(new SingleSource(new PathScript(Source))).run();
        assertFalse(Files.exists(TargetSource));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Source);
        Files.deleteIfExists(TargetHeader);
        Files.deleteIfExists(TargetSource);
    }
}
