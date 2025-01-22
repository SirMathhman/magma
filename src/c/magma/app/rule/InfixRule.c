import magma.api.result.Err;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.Optional;struct InfixRule implements Rule {
const Rule leftRule;
const Locator locator;
const Rule rightRule;
}