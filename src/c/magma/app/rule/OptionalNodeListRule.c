import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.rule.divide.DivideRule;import java.util.List;struct OptionalNodeListRule implements Rule {
const String propertyKey;
const Rule ifPresent;
const Rule ifEmpty;
const OrRule rule;
}