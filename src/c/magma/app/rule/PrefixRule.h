import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
public struct PrefixRule implements Rule {private final String prefix;private final Rule childRule;public PrefixRule(String prefix,  Rule childRule){this.prefix =prefix;this.childRule =childRule;}public static Result<String, CompileError> truncateLeft(String input,  String slice){if (input.startsWith(slice)) return new Ok<>(input.substring(slice.length()));return new Err<>(new CompileError("Prefix '" + slice + "' not present", new StringContext(input)));}@Override
    public Result<Node, CompileError> parse(String input){return truncateLeft(input, this.prefix).flatMapValue(this.childRule::parse);}@Override
    public Result<String, CompileError> generate(Node node){return this.childRule.generate(node).mapValue(inner ->this.prefix + inner);}}