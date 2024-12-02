package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.ExpandAssign;
import magma.app.compile.lang.casm.ExpandMove;
import magma.app.compile.lang.magma.FilteredStateful;
import magma.app.compile.lang.magma.MagmaLang;
import magma.app.compile.lang.magma.ParseNumeric;
import magma.app.compile.lang.magma.WrapRoot;
import magma.app.compile.pass.*;
import magma.java.JavaList;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    private static PassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateful("numeric-type", new ParseNumeric())))))
                .add(new WrapRoot())
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateful("initialize", new ExpandDeclare()))
                        .add(new FilteredStateful("assign", new ExpandAssign()))
                        .add(new FilteredStateful("move", new ExpandMove())))))
                .add(new Setup()));
    }

    public static Result<Node, CompileError> compile(String source) {
        final var passingStage = createPassingStage();

        return MagmaLang.createMagmaRootRule()
                .parse(source)
                .flatMapValue(root -> passingStage.pass(new State(), root))
                .mapValue(Tuple::right);
    }
}