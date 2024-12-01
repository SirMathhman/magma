package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.CASMLang;
import magma.app.compile.lang.common.FlattenBlock;
import magma.app.compile.lang.common.FlattenGroup;
import magma.app.compile.lang.magma.*;
import magma.app.compile.pass.*;
import magma.app.compile.resolve.ResolveDeclaration;
import magma.java.JavaList;

import static magma.app.compile.lang.casm.CASMLang.PROGRAM_CHILDREN;
import static magma.app.compile.lang.casm.CASMLang.PROGRAM_TYPE;
import static magma.app.compile.lang.magma.MagmaLang.ROOT_TYPE;
import static magma.app.compile.pass.Starter.START_LABEL;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    private static PassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .add(new WrapFunction())
                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>().add(new ResolveDeclaration()))))
                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>()
                        .add(new Definer())
                        .add(new TagSymbol())
                        .add(new FlattenDeclaration())
                        .add(new FlattenAssignment()))))

                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>()
                        .add(new FlattenGroup())
                        .add(new FlattenBlock()))))

                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>().add(new MyPasser()))))
                .add(new Starter()));
    }

    public static Result<Node, CompileError> compile(String source) {
        final var passingStage = createPassingStage();

        return MagmaLang.createMagmaRootRule()
                .parse(source)
                .flatMapValue(root -> passingStage.pass(new State(), root))
                .mapValue(Tuple::right);
    }

    private static class MyPasser implements Passer {
        @Override
        public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
            if (!node.is(ROOT_TYPE)) return new None<>();

            final var children = node.findNodeList(ROOT_CHILDREN).orElse(new JavaList<>());
            final var label = CASMLang.label(START_LABEL, children.list());
            final var programChildren = new JavaList<Node>().add(label);
            final var program = new MapNode(PROGRAM_TYPE).withNodeList(PROGRAM_CHILDREN, programChildren);
            return new Some<>(new Ok<>(new Tuple<>(state, program)));
        }
    }
}