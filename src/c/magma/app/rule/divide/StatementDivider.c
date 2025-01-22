import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import magma.app.rule.Splitter;import java.util.ArrayList;import java.util.LinkedList;import java.util.List;import java.util.stream.Collectors;import java.util.stream.IntStream;struct StatementDivider implements Divider{
	Divider STATEMENT_DIVIDER=StatementDivider.new();
	private StatementDivider(){}
	String merge(String current, String value){
		return current+value;
	}
	Result<List<String>, CompileError> divide(String input){
		var segments=ArrayList<String>.new();
		var buffer=StringBuilder.new();
		var depth=0;
		var queue=IntStream.range(0, input.length()).mapToObj(input::charAt).collect(Collectors.toCollection(LinkedList::new));
		while(!queue.isEmpty()){
			var c=queue.pop();
			buffer.append(c);
			if(c=='\''){
				var c1=queue.pop();
				buffer.append(c1);
				if(c1=='\\'){
					buffer.append(queue.pop());
				}
				buffer.append(queue.pop());
				continue;
			}
			if(c=='"'){
				while(!queue.isEmpty()){
					var c1=queue.pop();
					buffer.append(c1);
					if(c1=='"')break;
					if(c1=='\\'){
						buffer.append(queue.pop());
					}
				}
				continue;
			}
			if(c==';'&&depth==0){
				Splitter.advance(buffer, segments);
				buffer=StringBuilder.new();
			}
			else if(c=='}'&&depth==1){
				depth--;
				Splitter.advance(buffer, segments);
				buffer=StringBuilder.new();
			}
			else{
				if(c=='{' || c == '(')depth++;
				if(c=='}' || c == ')')depth--;
			}
		}
		Splitter.advance(buffer, segments);
		if(depth==0){
			return Ok<>.new();
		}
		else{
			return Err<>.new();
		}
	}
}