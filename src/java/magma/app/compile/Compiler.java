package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.MagmaLang;
import magma.java.JavaLists;
import magma.java.JavaStreams;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(List.of(
                new Passer() {
                    @Override
                    public Option<Result<Node, CompileError>> afterPass(Node node) {
                        if (!node.is(ROOT_TYPE)) return new None<>();
                        return new Some<>(new Ok<>(node.retype(FUNCTION_TYPE)));
                    }
                }
        ));
    }

    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(List.of(
                new TreePassingStage(createPasser()),
                new TreePassingStage(createFormattingPasser())
        ));
    }

    private static Passer createFormattingPasser() {
        return new Passer() {
            private static Result<List<Node>, CompileError> prependChildren(List<Node> children) {
                return new Ok<>(JavaStreams.fromList(children)
                        .map(child -> child.withString(BLOCK_BEFORE_CHILD, "\n\t"))
                        .collect(JavaLists.collector()));
            }

            @Override
            public Option<Result<Node, CompileError>> afterPass(Node node) {
                if (!node.is(BLOCK_TYPE)) return new None<>();

                return new Some<>(node
                        .mapNodeList(BLOCK_CHILDREN, children -> prependChildren(children))
                        .orElse(new Ok<>(node)));
            }
        };
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CLang.createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(createPassingStage()::pass)
                .flatMapValue(targetRule::generate);
    }
}