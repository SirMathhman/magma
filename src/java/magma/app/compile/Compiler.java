package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.HeadedStream;
import magma.api.stream.SingleHead;
import magma.api.stream.Stream;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.CASMLang;
import magma.app.compile.lang.casm.LoadNumeric;
import magma.app.compile.lang.magma.FlattenAssignment;
import magma.app.compile.lang.magma.FlattenDeclaration;
import magma.app.compile.lang.magma.MagmaLang;
import magma.app.compile.lang.magma.WrapFunction;
import magma.app.compile.pass.*;
import magma.app.compile.resolve.ResolveDeclaration;
import magma.java.JavaList;

import static magma.app.compile.lang.casm.CASMLang.PROGRAM_CHILDREN;
import static magma.app.compile.lang.casm.CASMLang.PROGRAM_TYPE;
import static magma.app.compile.lang.magma.CommonLang.GROUP_CHILDREN;
import static magma.app.compile.lang.magma.CommonLang.GROUP_TYPE;
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
                        .add(new FlattenDeclaration())
                        .add(new FlattenAssignment()))))

                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>().add(new LoadNumeric()))))
                .add(new TreePassingStage(new FlattenGroup()))

                .add(new TreePassingStage(new CompoundPasser(new JavaList<Passer>().add(new MyPasser()))))
                .add(new Starter()));
    }

    public static Result<Node, CompileError> compile(String source) {
        final var passingStage = createPassingStage();

        return MagmaLang.createMagmaRootRule()
                .parse(source)
                .flatMapValue(passingStage::pass);
    }

    private static class MyPasser implements Passer {
        @Override
        public Option<Result<Node, CompileError>> afterNode(Node node) {
            if (!node.is(ROOT_TYPE)) return new None<>();

            final var children = node.findNodeList(ROOT_CHILDREN).orElse(new JavaList<>());
            final var label = CASMLang.label(START_LABEL, children.list());
            final var programChildren = new JavaList<Node>().add(label);
            final var program = new MapNode(PROGRAM_TYPE).withNodeList(PROGRAM_CHILDREN, programChildren);
            return new Some<>(new Ok<>(program));
        }
    }

    private static class FlattenGroup implements Passer {
        @Override
        public Option<Result<Node, CompileError>> afterNode(Node node) {
            if (!node.is(GROUP_TYPE)) return new None<>();

            return node.mapNodeList(GROUP_CHILDREN, children -> new Ok<>(children.stream()
                    .flatMap(this::flatten)
                    .foldLeft(new JavaList<>(), JavaList::add)));
        }

        private Stream<Node> flatten(Node child) {
            if (!child.is(GROUP_TYPE)) return new HeadedStream<>(new SingleHead<>(child));

            return child.findNodeList(GROUP_CHILDREN)
                    .orElse(new JavaList<>())
                    .stream();
        }
    }
}