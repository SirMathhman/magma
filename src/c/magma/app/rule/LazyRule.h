import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.Context;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.Optional;struct LazyRule implements Rule {
Optional<Rule> childRule=Optional.empty();
}