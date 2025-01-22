import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import java.util.List;struct OptionalNodeRule implements Rule {
const String propertyKey;
const Rule ifPresent;
const Rule ifEmpty;
const OrRule rule;
}