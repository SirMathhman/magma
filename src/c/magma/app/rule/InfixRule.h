import magma.api.result.Err;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import magma.app.rule.locate.Locator;import java.util.ArrayList;import java.util.Optional;struct InfixRule implements Rule{
Rule leftRule;
Locator locator;
Rule rightRule;
public InfixRule(Rule leftRule, Locator locator, Rule rightRule);
ArrayList<Integer> add(ArrayList<Integer> integers, Integer integer);
Result<String, CompileError> generate(Node node);
Result<Node, CompileError> parse(String input);
}