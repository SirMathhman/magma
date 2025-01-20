import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
public struct ExactRule(String slice) implements Rule {@Override
    public Result<NodeCompileError> parse(String input){if(input.equals(this.slice)) return new Ok<>(new MapNode());final var context =new StringContext(input);return new Err<>(new CompileError("Exact string '" + this.slice + "' was not present"context));}@Override
    public Result<StringCompileError> generate(Node node){return new Ok<>(this.slice);}}