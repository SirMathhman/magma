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

private StatementDivider(){
}

String merge(String current, String value){
	return current+value;
}

Result<List<String>, CompileError> divide(String input){
	const auto segments=new ArrayList<String>();
	auto buffer=new StringBuilder();
	auto depth=0;
	const auto queue=IntStream.range(0, input.length()).mapToObj(input.charAt).collect(Collectors.toCollection(LinkedList.new));
	while(!queue.isEmpty()){
		const auto c=queue.pop();
		buffer.append(c);
		if(c=='\''){
			const auto c1=queue.pop();
			buffer.append(c1);
			if(c1=='\\'){
				buffer.append(queue.pop());
			}
			buffer.append(queue.pop());
			continue;
		}
		if(c=='"'){
			while(!queue.isEmpty()){
				const auto c1=queue.pop();
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
			buffer=new StringBuilder();
		}
		else if(c=='}'&&depth==1){
			depth--;
			Splitter.advance(buffer, segments);
			buffer=new StringBuilder();
		}
		else {
			if(c=='{' || c == '(')depth++;
			if(c=='}' || c == ')')depth--;
		}
	}
	Splitter.advance(buffer, segments);
	if(depth==0){
		return new Ok<>(segments);
	}
	else {
		return new Err<>(new CompileError("Invalid depth '"+depth+"'", new StringContext(input)));
	}
}
struct StatementDivider implements Divider {static const Divider STATEMENT_DIVIDER=new StatementDivider();
}
