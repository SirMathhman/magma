package magma.app.compile.lang.casm;

import magma.app.compile.Node;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

import static magma.app.compile.Compiler.SPILL;
import static magma.app.compile.Compiler.STACK_POINTER;
import static magma.app.compile.lang.casm.CASMLang.*;
import static magma.app.compile.lang.casm.assemble.Operator.*;

public class ExpandMoveStackPointer implements Stateless {
    @Override
    public Node beforePass(Node node) {
        final var offset = node.findInt("offset").orElse(0);

        if(offset == 0) return CommonLang.createEmptyGroup();

        var instruction = offset < 0
                ? instruct(SubtractFromValue, offset)
                : instruct(AddFromValue, offset);

        return CommonLang.asGroup(new JavaList<Node>()
                .add(instruct(StoreDirectly, SPILL))
                .add(instruct(LoadDirectly, STACK_POINTER))
                .add(instruction)
                .add(instruct(StoreDirectly, STACK_POINTER))
                .add(instruct(LoadDirectly, SPILL)));
    }
}
