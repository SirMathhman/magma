package magma;

import magma.api.Results;
import magma.compile.CompileException;
import magma.compile.lang.ClassSplitter;
import magma.compile.lang.JavaLang;
import magma.compile.lang.MagmaFormatter;
import magma.compile.lang.MagmaLang;
import magma.compile.lang.MethodRenamer;
import magma.compile.lang.Modifier;
import magma.compile.lang.ModifierAttacher;
import magma.compile.lang.RootTypeRemover;
import magma.compile.rule.Node;
import magma.compile.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";

    public static void main(String[] args) {
        try {
            var source = Paths.get(".", "magmac", "src", "magma", "Main.java");
            var input = Files.readString(source);
            var target = source.resolveSibling("Main.mgs");
            var root = JavaLang.createRootRule().toNode(input).create().orElseThrow();

            Files.writeString(source.resolveSibling("Main.input.ast"), root.toString());
            var generated = generate(root);
            Files.writeString(source.resolveSibling("Main.output.ast"), generated.toString());

            Rule rule = MagmaLang.createRootRule();
            Files.writeString(target, Results.unwrap(rule.fromNode(generated)));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        } catch (CompileException e) {
            print(e, 0);
        }
    }

    private static void print(CompileException e, int depth) {
        var message = e.getMessage();
        System.err.println("\t".repeat(depth) + message);

        var cause = e.getCause();
        if (cause == null) {
            System.err.println("\n---\n" + e.content + "\n---\n");
        } else {
            if (cause instanceof CompileException cast) {
                print(cast, depth + 1);
            } else {
                cause.printStackTrace();
            }
        }
    }

    private static Node generate(Node root) {
        var list = Arrays.asList(
                new RootTypeRemover("package"),
                new RootTypeRemover("whitespace"),
                new MethodRenamer(),
                new ModifierAttacher(),
                new ClassSplitter(),
                new MagmaFormatter()
        );

        Node acc = root;
        for (Modifier modifier : list) {
            acc = modifier.generate(acc);
        }
        return acc;
    }
}
