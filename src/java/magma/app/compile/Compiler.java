package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.c.CLang;
import magma.app.compile.lang.magma.MagmaLang;
import magma.app.compile.lang.magma.WrapRoot;
import magma.app.compile.pass.*;
import magma.java.JavaList;

import java.nio.file.Paths;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";
    public static final String SPILL0 = "__spill0__";

    private static PassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .add(new WrapRoot())
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>())))
                .add(new SavingPassingStage(Paths.get(".", "main.c"), CLang.createRootRule()))
        );

        /*
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("pointer", new ParsePointer()))
                        .add(new FilteredStateless("signed-numeric-type", new ParsePrimitiveType()))
                        .add(new FilteredStateless("unsigned-numeric-type", new ParsePrimitiveType()))
                        .add(new FilteredStateless("boolean", new ParsePrimitiveType()))
                )))
                .add(new WrapRoot())
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("whitespace", new ExpandWhitespace()))
                        .add(new FilteredStateless("block", new ExpandBlock(new Generator())))
                        .add(new FilteredStateless("initialize", new ExpandInitialize()))
                        .add(new FilteredStateless("assign", new ExpandAssign()))
                        .add(new FilteredStateless("move", new ExpandMove())))))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new Definer())
                        .add(new ResolveSymbol())
                )))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("numeric-value", new ResolveNumericValue()))
                        .add(new FilteredStateless("address", new ResolveAddress()))

                        .add(new FilteredStateless("add", new ResolveAdd()))
                        .add(new FilteredStateless("subtract", new ResolveSubtract()))
                        .add(new FilteredStateless("reference", new ResolveReference()))
                        .add(new FilteredStateless("dereference", new ResolveDereference()))

                        .add(new FilteredStateless("less-than", new ResolveLessThan()))

                        .add(new FilteredStateless("store", new ResolveStore()))
                        .add(new FilteredStateless("move-stack-pointer", new ExpandMoveStackPointer())))))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("function", new FlattenFunction()))
                        .add(new FilteredStateless("group", new FlattenGroup()))
                        .add(new FilteredStateless("block", new FlattenBlock())))))
                .add(new TreePassingStage(new CompoundStateful(new JavaList<Stateful>()
                        .add(new FilteredStateless("label", new FlattenLabel())))))
                .add(new Setup()));*/
    }

    public static Result<Node, CompileError> compile(String source) {
        final var passingStage = createPassingStage();

        return MagmaLang.createMagmaRootRule()
                .parse(source)
                .flatMapValue(root -> passingStage.pass(new State(), root))
                .mapValue(Tuple::right);
    }
}