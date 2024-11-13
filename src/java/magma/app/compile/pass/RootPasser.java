package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.Passer;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;

import java.util.Collections;
import java.util.List;

import static magma.Assembler.MAIN;
import static magma.Assembler.SECTION_PROGRAM;
import static magma.app.compile.lang.CASMLang.*;

public class RootPasser implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        final var labelValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, Collections.emptyList())
                .withString(BLOCK_AFTER_CHILDREN, "\n\t");

        final var label = new MapNode(LABEL_TYPE)
                .withString(GROUP_NAME, MAIN)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withNode(GROUP_VALUE, labelValue);

        final var sectionValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, List.of(label))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var section = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, SECTION_PROGRAM)
                .withString(GROUP_AFTER_NAME, " ")
                .withNode(GROUP_VALUE, sectionValue);

        return new Some<>(new Ok<>(new Tuple<>(state, new MapNode(ROOT_TYPE).withNodeList(CHILDREN, List.of(section)))));
    }
}
