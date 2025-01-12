import magma.collect.List;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.Strings;
struct ValueSplitter implements Splitter {
	StringBuilder merge(StringBuilder inner, String compiled){
		return inner.append(", ").append(compiled);
	}
	List<String> split(String input){
		auto inputParamsJavaList = JavaList<String>();
		auto buffer = StringBuilder();
		auto depth = 0;
		auto queue = Strings.streamChars(input).collect(JavaLinkedList.collector());
		while (!queue.isEmpty()) {
			auto c = queue.popOrPanic();
			if (c == ',' && depth == 0) {
				Splitter.advance(inputParamsJavaList, buffer);
				buffer = StringBuilder();
			}
			else {
				buffer.append(c);
				if (c == ' - ') {
					if (!queue.isEmpty() && queue.peek().filter(auto _lambda57_(Some[value=auto value]){
						return value == '>';
					}).isPresent()) {
						buffer.append(queue.popOrPanic());
					}
				}
				if (c == '<' || c == '(') {
					depth++;
				}
				if (c == '>' || c == ')') {
					depth--;
				}
			}
		}
		Splitter.advance(inputParamsJavaList, buffer);
		return inputParamsJavaList;
	}
}