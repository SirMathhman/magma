import magma.api.result.Err;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.rule.Rule;import java.util.ArrayList;import java.util.List;import java.util.Optional;import java.util.function.Function;struct DivideRule implements Rule {
const String propertyKey;
const Divider divider;
const Rule childRule;
}