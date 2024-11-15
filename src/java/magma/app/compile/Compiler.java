package magma.app.compile;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.format.SectionFormatter;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.lang.casm.DataFormatter;
import magma.app.compile.lang.casm.StatelessFolder;
import magma.app.compile.lang.casm.TypePasser;
import magma.app.compile.pass.InstructionWrapper;
import magma.java.JavaList;

import static magma.app.compile.lang.CASMLang.DATA_TYPE;
import static magma.app.compile.lang.CASMLang.SECTION_TYPE;

public record Compiler(String input) {
    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(new JavaList<PassingStage>()
                .addLast(createAssemblyStage())
                .addLast(createFormattingStage()));
    }

    private static TreePassingStage createFormattingStage() {
        return new TreePassingStage(new CompoundPasser(new JavaList<Passer>()
                .addLast(new TypePasser(DATA_TYPE, new StatelessFolder(new DataFormatter())))
                .addLast(new TypePasser(SECTION_TYPE, new StatelessFolder(new SectionFormatter())))));
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