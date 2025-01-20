import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.Splitter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public struct ValueDivider implements Divider {public static final Divider VALUE_DIVIDER = new ValueDivider();private ValueDivider(){}@Override
    public String merge(String current,  String value){return current+", "+value;}@Override
    public Result<List<String>, CompileError> divide(String input){final var segments =new ArrayList<String>();var buffer =new StringBuilder();var depth =0;final var queue =IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));while(!queue.isEmpty()){final var c =queue.pop();if(c=='\''){buffer.append(c);if(queue.isEmpty()){return new Err<>(new CompileError("Malformed chars", new StringContext(input)));}final var c1 =queue.pop();buffer.append(c1);if(c1=='\\'){buffer.append(queue.pop());}buffer.append(queue.pop());}if(c=='\"'){buffer.append(c);while(!queue.isEmpty()){final var next =queue.pop();buffer.append(next);if(next=='\\') buffer.append(queue.pop());if(next=='\"') break;}continue;}if(c==','&&depth==0){Splitter.advance(buffer, segments);buffer =new StringBuilder();}else {buffer.append(c);if(c=='<' || c == '(') depth++;if(c=='>' || c == ')') depth--;}}Splitter.advance(buffer, segments);return new Ok<List<String>, CompileError>(segments);}}