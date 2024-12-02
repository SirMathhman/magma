package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.magma.MagmaLang;
import magma.app.compile.lang.magma.ParseNumericType;
import magma.app.compile.lang.magma.WrapRoot;
import magma.app.compile.pass.*;
import magma.java.JavaList;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    private static PassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>().add(new ParseNumericType()))))
                .add(new WrapRoot())
                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>()
                        .add(new Passer() {
                            @Override
                            public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
                                return new None<>();
                            }
                        }))))
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