import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.rule.Splitter;
import java.util.ArrayList;
import java.util.List;
public struct ValueDivider implements Divider {public static final Divider VALUE_DIVIDER = new ValueDivider();private ValueDivider(){}@Override
    public String merge(String current,  String value){return current+", "+value;}@Override
    public Result<List<String>, CompileError> divide(String input){final var segments =new ArrayList<String>();var buffer =new StringBuilder();var depth =0;int i =0;while(i<input.length()){final var c =input.charAt(i);if(c==','&&depth==0){Splitter.advance(buffer, segments);buffer =new StringBuilder();}else {buffer.append(c);if(c=='<' || c == '(') depth++;if(c=='>' || c == ')') depth--;}i++;}Splitter.advance(buffer, segments);return new Ok<List<String>, CompileError>(segments);}}