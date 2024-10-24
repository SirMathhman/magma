package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.rule.Rule;
import magma.app.compile.rule.RuleResult;

import java.util.List;

import static magma.app.compile.lang.JavaLang.*;

public record Compiler(String input) {

    public Result<CompileResult, CompileException> compile() {
        Rule rule = createRootRule();
        final var parsed = rule.parse(input);
        return write(parsed).flatMapValue(beforePass -> {
            final var afterPass = Passer.pass(beforePass);
            final var generated = MagmaLang.createRootRule().generate(afterPass);
            return write(generated).mapValue(output -> new CompileResult(beforePass, afterPass, output));
        });
    }

    private <T, E extends Exception> Result<T, CompileException> write(RuleResult<T, E> result) {
        if (result.isValid()) {
            return new Ok<>(result.result().findValue().orElseThrow());
        } else {
            writeResult(result, 0, 0);
            return new Err<>(new CompileException());
        }
    }

    private <T, E extends Exception> void writeResult(RuleResult<T, E> result, int depth, int index) {
        final var error = result.result().findError();
        if (error.isPresent()) {
            final var repeat = "| ".repeat(depth);
            final var s = (index + 1) + ") ";
            final var rawMessage = error.get().getMessage();
            final var message = rawMessage.replaceAll("\r\n", "\r\n" + repeat + " ".repeat(s.length()));
            System.out.println(repeat + s + message);
        }

        List<RuleResult<T, E>> children = result.sortedChildren();
        int i = 0;
        while (i < children.size()) {
            RuleResult<T, E> child = children.get(i);
            writeResult(child, depth + 1, i);
            i++;
        }
    }
}