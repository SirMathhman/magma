package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;

import java.util.List;

public record Compiler(String input) {
    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(List.of(
                new PassingStage() {
                    @Override
                    public Result<Tuple<State, Node>, CompileError> pass(State state, Node node) {
                        return new Ok<>(new Tuple<>(state, node));
                    }
                }
        ));
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CASMLang.createRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(node -> createPassingStage().pass(new State(), node))
                .mapValue(Tuple::right)
                .flatMapValue(targetRule::generate);
    }

}