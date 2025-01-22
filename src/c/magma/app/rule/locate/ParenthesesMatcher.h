import magma.api.Tuple;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Override
String unwrap(){
	return ")";
}

@Override
int length(){
	return 1;
}

@Override
Stream<Integer> locate(String input){
	auto depth=0;
	const auto queue=IntStream.range(0, input.length()).mapToObj(index -> new Tuple<>(index, input.charAt(index)))
                .collect(Collectors.toCollection(LinkedList.new));
	while(!queue.isEmpty()){
		const auto tuple=queue.pop();
		const auto i=tuple.left();
		const auto c=tuple.right();
		if(c=='\''){
			const auto tuple1=queue.pop();
			if(tuple1.right() == '\\'){
				queue.pop();
			}
			queue.pop();
		}
		if(c==')'&&depth==1)return Streams.of(i);
		if(c=='(')depth++;
		if(c==')')depth--;
	}
	return Streams.empty();
}
struct ParenthesesMatcher implements Locator {
}

