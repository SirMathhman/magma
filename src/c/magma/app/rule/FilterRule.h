import magma.api.result.Err;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import java.util.function.Predicate;struct FilterRule(Predicate<String> filter,
                         Rule childRule) implements Rule {
}