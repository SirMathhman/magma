package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;

import java.util.Collections;
import java.util.List;

import static magma.Assembler.MAIN;
import static magma.Assembler.SECTION_PROGRAM;
import static magma.app.compile.lang.CASMLang.*;

public record Compiler(String input) {
    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(List.of(
                new TreePassingStage(new CompoundPasser(List.of(
                        new Passer() {
                            @Override
                            public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
                                final var labelValue = new MapNode(BLOCK_TYPE)
                                        .withNodeList(CHILDREN, Collections.emptyList());

                                final var label = new MapNode(LABEL_TYPE)
                                        .withString(GROUP_NAME, MAIN)
                                        .withNode(GROUP_VALUE, labelValue);

                                final var sectionValue = new MapNode(BLOCK_TYPE)
                                        .withNodeList(CHILDREN, List.of(label));

                                final var section = new MapNode(SECTION_TYPE)
                                        .withString(GROUP_NAME, SECTION_PROGRAM)
                                        .withNode(GROUP_VALUE, sectionValue);

                                return new Some<>(new Ok<>(new Tuple<>(state, new MapNode(ROOT_TYPE).withNodeList(CHILDREN, List.of(section)))));
                            }
                        }
                )))
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