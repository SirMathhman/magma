package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.*;
import magma.app.compile.lang.common.FlattenGroup;
import magma.app.compile.lang.magma.*;
import magma.app.compile.pass.*;
import magma.java.JavaList;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    private static PassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("numeric-type", new ParseNumeric())))))
                .add(new WrapRoot())
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("initialize", new ExpandDeclare()))
                        .add(new FilteredStateless("assign", new ExpandAssign()))
                        .add(new FilteredStateless("move", new ExpandMove())))))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new Definer())
                        .add(new Resolver()))))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("load", new ResolveLoad()))
                        .add(new FilteredStateless("store", new ResolveStore()))
                        .add(new FilteredStateless("move-stack-pointer", new ExpandMoveStackPointer())))))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("group", new FlattenGroup()))
                        .add(new FilteredStateless("function", new FlattenFunction())))))
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