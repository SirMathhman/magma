package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.MagmaLang;

import java.util.List;

import static magma.app.compile.lang.CommonLang.FUNCTION_TYPE;
import static magma.app.compile.lang.CommonLang.ROOT_TYPE;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(List.of(
                new Passer() {
                    @Override
                    public Option<Result<Node, CompileError>> afterPass(Node node) {
                        if(!node.is(ROOT_TYPE)) return new None<>();



                        return new Some<>(new Ok<>(node.retype(FUNCTION_TYPE)));
                    }
                }
        ));
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CLang.createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(new TreePassingStage(createPasser())::pass)
                .flatMapValue(targetRule::generate);
    }
}