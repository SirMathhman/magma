package magma;

import magma.api.Results;
import magma.compile.CompileException;
import magma.compile.lang.JavaLang;
import magma.compile.lang.MagmaLang;
import magma.compile.lang.MethodRenamer;
import magma.compile.lang.Modifier;
import magma.compile.lang.ObjectSplitter;
import magma.compile.lang.RootTypeRemover;
import magma.compile.rule.Node;
import magma.compile.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";

    public static void main(String[] args) {
        try {
            var source = Paths.get(".", "magmac", "src", "magma", "Main.java");
            var input = Files.readString(source);
            var target = source.resolveSibling("Main.mgs");
            var root = JavaLang.createRootRule().toNode(input).create().orElseThrow();
            var generated = generate(root);
            Rule rule = MagmaLang.createRootRule();
            Files.writeString(target, Results.unwrap(rule.fromNode(generated)));
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Node generate(Node root) {
        var list = Arrays.asList(
                new RootTypeRemover("package"),
                new RootTypeRemover("whitespace"),
                new MethodRenamer(),
                new ObjectSplitter()
        );

        Node acc = root;
        for (Modifier modifier : list) {
            acc = modifier.generate(acc);
        }
        return acc;
    }
}
