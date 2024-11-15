package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.*;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.pass.*;
import magma.java.JavaList;

import static magma.app.compile.lang.CASMLang.*;

public record Compiler(String input) {
    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .addLast(createAssemblyStage())
                .addLast(createFormattingStage()));
    }

    private static TreePassingStage createFormattingStage() {
        final var passers = new JavaList<Passer>()
                .addLast(new TypePasser(SECTION_TYPE, new StatelessFolder(new SectionFormatter())))
                .addLast(new TypePasser(DATA_TYPE, new DataFormatter()))
                .addLast(new TypePasser(LABEL_TYPE, new LabelFormatter()))
                .addLast(new TypePasser(BLOCK_TYPE, new BlockFormatter()))
                .addLast(new TypePasser(INSTRUCTION_TYPE, new InstructionFormatter()))
                .addLast(new TypePasser(COMMENT_TYPE, new CommentFormatter()));

        return new TreePassingStage(new CompoundPasser(passers));
    }

    private static TreePassingStage createAssemblyStage() {
        return new TreePassingStage(new CompoundPasser(new JavaList<Passer>()
                .addLast(new InstructionWrapper())));
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