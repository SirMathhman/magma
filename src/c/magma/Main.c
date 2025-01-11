import magma.java.JavaPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
struct Main {
	Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
	Path TARGET_DIRECTORY = Paths.get(".", "src", "c");
	int counter = 0;
	void main(Slice<String> args){
		JavaPaths.collect().match(Main.compileSources, Optional.of).ifPresent(Throwable.printStackTrace);
	}
	Optional<IOException> compileSources(Set<Path> sources){
		return sources.stream().map(Main.compileSource).flatMap(Optional.stream).findFirst();
	}
	Optional<IOException> compileSource(Path source){
		auto relative = SOURCE_DIRECTORY.relativize(source);
		auto parent = relative.getParent();
		auto namespace = computeNamespace(parent);
		auto name = computeName(relative);
		if (namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))) {
			return Optional.empty();
		}
		auto targetParent = namespace.stream().reduce(TARGET_DIRECTORY, Path.resolve, auto _lambda0_(auto _, auto next){
			return next;
		});
		auto target = targetParent.resolve(name + ".c");
		return ensureDirectory(targetParent).or(auto _lambda1_(){
			return compileFromSourceToTarget(source, target);
		});
	}
	Optional<IOException> ensureDirectory(Path targetParent){
		if (Files.exists(targetParent)) {
			return Optional.empty();
		}
		return JavaPaths.createDirectoriesSafe(targetParent);
	}
	Optional<IOException> compileFromSourceToTarget(Path source, Path target){
		return JavaPaths.readSafe(source).mapValue(Main.compile).match(auto _lambda2_(auto output){
			return JavaPaths.writeSafe(target, output);
		}, Optional.of);
	}
	String computeName(Path relative){
		auto name = relative.getFileName().toString();
		return name.substring(0, name.indexOf('.'));
	}
	List<String> computeNamespace(Path parent){
		return IntStream.range(0, parent.getNameCount()).mapToObj(parent.getName).map(Path.toString).toList();
	}
	String compile(String root){
		return splitAndCompile(Main.splitByStatements, Main.compileRootMember, Main.mergeStatements, root);
	}
	String splitAndCompile(Function<String, List<String>> splitter, Function<String, String> compiler, BiFunction<StringBuilder, String, StringBuilder> merger, String input){
		return splitter.apply(input).stream().map(String.strip).filter(auto _lambda5_(auto value){
			return !value.isEmpty();
		}).reduce(Optional.<StringBuilder>empty(), auto _lambda3_(auto output, auto stripped){
			return compileAndMerge(compiler, merger, output, stripped);
		}, auto _lambda4_(auto _, auto next){
			return next;
		}).map(StringBuilder.toString).orElse("");
	}
	Optional<StringBuilder> compileAndMerge(Function<String, String> compiler, BiFunction<StringBuilder, String, StringBuilder> merger, Optional<StringBuilder> output, String stripped){
		auto compiled = compiler.apply(stripped);
		if (output.isEmpty()) {
			return Optional.of(StringBuilder(compiled));
		}
		else {
		}
	}
	StringBuilder mergeStatements(StringBuilder inner, String stripped){
		return inner.append(stripped);
	}
	List<String> splitByStatements(String root){
		auto segments = ArrayList<String>();
		auto buffer = StringBuilder();
		auto depth = 0;
		auto queue = IntStream.range(0, root.length()).mapToObj(root.charAt).collect(Collectors.toCollection(LinkedList.new));
		while (!queue.isEmpty()) {
			auto c = queue.pop();
			buffer.append(c);
			if (c == '\'') {
				auto popped = queue.pop();
				buffer.append(popped);
				if (popped == '\\') {
					buffer.append(queue.pop());
				}
				buffer.append(queue.pop());
				continue;
			}
			if (c == '"') {
				while (!queue.isEmpty()) {
					auto next = queue.pop();
					buffer.append(next);
					if (next == '"') {
						break;
					}
					if (next == '\\') {
						buffer.append(queue.pop());
					}
				}
			}
			if (c == ';' && depth == 0) {
				advance(segments, buffer);
				buffer = StringBuilder();
			}
		else {
		}
		else {
		}
		}
		advance(segments, buffer);
		return segments;
	}
	void advance(List<String> segments, StringBuilder buffer){
		if (!buffer.isEmpty()) {
			segments.add(buffer.toString());
		}
	}
	String compileRootMember(String rootSegment){
		if (rootSegment.startsWith("package ")) {
			return "";
		}
		if (rootSegment.startsWith("import ")) {
			return rootSegment + "\n";
		}
		auto classIndex = rootSegment.indexOf("class");
		if (classIndex != -1) {
			auto withoutKeyword = rootSegment.substring(classIndex + "class".length());
			auto contentStartIndex = withoutKeyword.indexOf("{");
			if (contentStartIndex != -1) {
				auto name = withoutKeyword.substring(0, contentStartIndex).strip();
				auto content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
				auto compiled = splitAndCompile(Main.splitByStatements, Main.compileClassSegment, Main.mergeStatements, content);
				return "struct " + name + " {" + compiled + "\n}";
			}
		}
		if (rootSegment.contains("record")) {
			return "struct Temp {\n}";
		}
		if (rootSegment.contains("interface ")) {
			return "struct Temp {\n}";
		}
		return invalidate("root segment", rootSegment);
	}
	String invalidate(String type, String rootSegment){
		System.err.println("Unknown " + type + ": " + rootSegment);
		return rootSegment;
	}
	String compileClassSegment(String classSegment){
		if (classSegment.endsWith(";")) {
			auto substring = classSegment.substring(0, classSegment.length() - 1);
			auto index = substring.indexOf('=');
			if (index != -1) {
				auto definition = substring.substring(0, index);
				auto compiled = compileValue(substring.substring(index + 1), 2);
				return "\n\t" + compileDefinition(definition).orElseGet(() -> invalidate("definition", definition)) + " = " + compiled + ";";
			}
		}
		auto paramStart = classSegment.indexOf('(');
		if (paramStart != -1) {
			auto beforeParamStart = classSegment.substring(0, paramStart);
			auto afterParamStart = classSegment.substring(paramStart + 1);
			auto paramEnd = afterParamStart.indexOf(')');
			if (paramEnd != -1) {
				auto nameSeparator = beforeParamStart.lastIndexOf(' ');
				if (nameSeparator != -1) {
					auto beforeName = beforeParamStart.substring(0, nameSeparator);
					auto typeSeparator = beforeName.lastIndexOf(' ');
					if (typeSeparator != -1) {
						auto type = beforeName.substring(typeSeparator + 1);
						auto name = beforeParamStart.substring(nameSeparator + 1);
						auto inputParams = afterParamStart.substring(0, paramEnd);
						auto afterParams = afterParamStart.substring(paramEnd + 1).strip();
						if (afterParams.startsWith("{") && afterParams.endsWith("}")) {
							auto inputContent = afterParams.substring(1, afterParams.length() - 1);
							auto outputContent = splitAndCompile(Main.splitByStatements, auto _lambda6_(auto statement){
			return compileStatement(statement, 2);
		}, Main.mergeStatements, inputContent);
							auto inputParamsList = splitByValues(inputParams);
							auto outputParams = compileParams(inputParamsList);
							return "\n\t" + type + " " + name + "(" + outputParams + "){" + outputContent + "\n\t}";
						}
					}
				}
			}
		}
		return invalidate("class segment", classSegment);
	}
	String compileStatement(String statement, int depth){
		if (statement.strip().equals("continue;")) {
			return generateStatement(depth, "continue");
		}
		if (statement.strip().equals("break;")) {
			return generateStatement(depth, "break");
		}
		if (statement.startsWith("for")) {
			return "\n\t\tfor (;;) {\n\t\t}";
		}
		if (statement.startsWith("else")) {
			return "\n\t\telse {\n\t\t}";
		}
		if (statement.startsWith("return ")) {
			auto substring = statement.substring("return ".length());
			if (substring.endsWith(";")) {
				auto substring1 = substring.substring(0, substring.length() - ";".length());
				auto compiled = compileValue(substring1, 2);
				return generateReturn(compiled, depth);
			}
		}
		auto optional1 = compileConditional(statement, depth, "while");
		if (optional1.isPresent()) {
			return optional1.get();
		}
		auto value = compileConditional(statement, depth, "if");
		if (value.isPresent()) {
			return value.get();
		}
		auto index1 = statement.indexOf("=");
		if (index1 != -1) {
			auto substring = statement.substring(0, index1);
			auto substring1 = statement.substring(index1 + 1);
			if (substring1.endsWith(";")) {
				auto compiled = compileDefinition(substring).or(auto _lambda8_(){
			return compileSymbol(substring);
		}).orElseGet(auto _lambda7_(){
			return invalidate("definition", substring);
		});
				auto compiled1 = compileValue(substring1.substring(0, substring1.length() - ";".length()).strip(), 2);
				return generateStatement(depth, compiled + " = " + compiled1);
			}
		}
		if (statement.endsWith(";")) {
			auto newCaller = compileInvocation(2, statement.substring(0, statement.length() - ";".length()));
			if (newCaller.isPresent()) {
				return generateStatement(depth, newCaller.get());
			}
		}
		if (statement.endsWith(";")) {
			auto optional = compileDefinition(statement.substring(0, statement.length() - 1));
			if (optional.isPresent()) {
				return optional.get();
			}
		}
		if (statement.endsWith("++;")) {
			auto substring = statement.substring(0, statement.length() - "++;".length());
			return compileValue(substring, 2) + "++;";
		}
		return invalidate("statement", statement);
	}
	String generateStatement(int depth, String content){
		return "\n" + "\t".repeat(depth) + content + ";";
	}
	String generateReturn(String compiled, int depth){
		return "\n" + "\t".repeat(depth) + "return " + compiled + ";";
	}
	Optional<String> compileConditional(String statement, int depth, String prefix){
		if (!statement.startsWith(prefix)) {
			return Optional.empty();
		}
		auto withoutKeyword = statement.substring(prefix.length());
		auto maybeParamEnd = findConditionParamEnd(withoutKeyword);
		if (maybeParamEnd.isEmpty()) {
			return Optional.empty();
		}
		auto paramEnd = maybeParamEnd.get();
		auto conditionWithEnd = withoutKeyword.substring(0, paramEnd).strip();
		auto content = withoutKeyword.substring(paramEnd + 1).strip();
		if (!conditionWithEnd.startsWith("(")) {
			return Optional.empty();
		}
		auto condition = conditionWithEnd.substring(1);
		auto value = compileValue(condition, 2);String outputContent
		if (content.startsWith("{") && content.endsWith("}")) {
			auto substring = content.substring(1, content.length() - 1);
			outputContent = splitAndCompile(Main.splitByStatements, auto _lambda9_(auto statement1){
			return compileStatement(statement1, depth + 1);
		}, Main.mergeStatements, substring);
		}
		else {
		}
		auto indent = "\n" + "\t".repeat(depth);
		return Optional.of(indent + prefix + " (" + value + ") {" + outputContent + indent + "}");
	}
	Optional<Integer> findConditionParamEnd(String input){
		auto queue = IntStream.range(0, input.length()).mapToObj(auto _lambda10_(auto index){
			return Tuple<>(index, input.charAt(index));
		}).collect(Collectors.toCollection(LinkedList.new));
		auto depth = 0;
		while (!queue.isEmpty()) {
			auto popped = queue.pop();
			auto i = popped.left();
			auto c = popped.right();
			if (c == '\'') {
				auto popped1 = queue.pop().right();
				if (popped1 == '\\') {
					queue.pop();
				}
				queue.pop();
			}
			if (c == '"') {
				while (!queue.isEmpty()) {
					auto next = queue.pop().right();
					if (next == '"') {
						break;
					}
					if (next == '\\') {
						queue.pop();
					}
				}
			}
			if (c == ')' && depth == 1) {
				return Optional.of(i);
			}
		else {
		}
		}
		return Optional.empty();
	}
	Optional<String> compileInvocation(int depth, String statement){
		auto stripped = statement.strip();
		if (!stripped.endsWith(")")) {
			return Optional.empty();
		}
		auto substring = stripped.substring(0, stripped.length() - ")".length());
		return findArgStart(substring).map(auto _lambda12_(auto index){
		auto caller = substring.substring(0, index);
		auto substring1 = substring.substring(index + 1);
		auto compiled = splitAndCompile(Main.splitByValues, auto _lambda11_(auto value){
			return compileValue(value.strip(), depth);
		}, Main.mergeValues, substring1);
		auto newCaller = compileValue(caller.strip(), depth);
		return newCaller + "(" + compiled + ")";
		});
	}
	Optional<Integer> findArgStart(String substring){
		auto depth = 0;
		for (;;) {
		}
		return Optional.empty();
	}
	String compileValue(String input, int depth){
		return compileSymbol(input).or(auto _lambda27_(){
			return compileNumber(input);
		}).or(auto _lambda26_(){
			return compileString(input);
		}).or(auto _lambda25_(){
			return compileChar(input);
		}).or(auto _lambda24_(){
			return compileNot(depth, input);
		}).or(auto _lambda23_(){
			return compileConstruction(depth, input);
		}).or(auto _lambda22_(){
			return compileLambda(depth, input);
		}).or(auto _lambda21_(){
			return compileInvocation(depth, input);
		}).or(auto _lambda20_(){
			return compileAccess(depth, input, ".");
		}).or(auto _lambda19_(){
			return compileAccess(depth, input, "::");
		}).or(auto _lambda18_(){
			return compileOperator(depth, input, "+");
		}).or(auto _lambda17_(){
			return compileOperator(depth, input, "==");
		}).or(auto _lambda16_(){
			return compileOperator(depth, input, "!=");
		}).or(auto _lambda15_(){
			return compileOperator(depth, input, "&&");
		}).or(auto _lambda14_(){
			return compileTernary(depth, input);
		}).orElseGet(auto _lambda13_(){
			return invalidate("value", input);
		});
	}
	Optional<String> compileNumber(String input){
		auto stripped = input.strip();
		if (isNumber(stripped)) {
			return Optional.of(stripped);
		}
		return Optional.empty();
	}
	Optional<String> compileNot(int depth, String input){
		if (input.startsWith("!")) {
			return Optional.of("!" + compileValue(input.substring(1), depth));
		}
		return Optional.empty();
	}
	Optional<String> compileString(String input){
		auto stripped = input.strip();
		if (stripped.startsWith("\"") && stripped.endsWith("\"")) {
			return Optional.of(stripped);
		}
		return Optional.empty();
	}
	Optional<String> compileChar(String input){
		auto stripped = input.strip();
		if (stripped.startsWith("'") && stripped.endsWith("'")) {
			return Optional.of(stripped);
		}
		return Optional.empty();
	}
	Optional<String> compileAccess(int depth, String input, String slice){
		auto index = input.lastIndexOf(slice);
		if (index == -1) {
			return Optional.empty();
		}
		auto substring = input.substring(0, index);
		auto substring1 = input.substring(index + slice.length());
		auto s = compileValue(substring, depth);
		return Optional.of(generateDataAccess(s, substring1));
	}
	String generateDataAccess(String s, String substring1){
		return s + "." + substring1;
	}
	Optional<String> compileTernary(int depth, String stripped){
		auto index3 = stripped.indexOf('?');
		if (index3 == -1) {
			return Optional.empty();
		}
		auto condition = stripped.substring(0, index3);
		auto substring = stripped.substring(index3 + 1);
		auto maybe = substring.indexOf(':');
		if (maybe == -1) {
			return Optional.empty();
		}
		auto substring1 = substring.substring(0, maybe);
		auto substring2 = substring.substring(maybe + 1);
		return Optional.of(compileValue(condition, depth) + " ? " + compileValue(substring1, depth) + " : " + compileValue(substring2, depth));
	}
	Optional<String> compileSymbol(String input){
		auto stripped = input.strip();
		if (isSymbol(stripped)) {
			return Optional.of(stripped);
		}
		return Optional.empty();
	}
	Optional<String> compileLambda(int depth, String input){
		auto arrowIndex = input.indexOf("->");
		if (arrowIndex == -1) {
			return Optional.empty();
		}
		auto beforeArrow = input.substring(0, arrowIndex).strip();
		auto afterArrow = input.substring(arrowIndex + "->".length()).strip();
		auto maybeNames = findLambdaNames(beforeArrow);
		if (maybeNames.isEmpty()) {
			return Optional.empty();
		}String compiled
		if (afterArrow.startsWith("{") && afterArrow.endsWith("}")) {
			auto substring1 = afterArrow.substring(1, afterArrow.length() - 1);
			compiled = splitAndCompile(Main.splitByStatements, auto _lambda28_(auto statement){
			return compileStatement(statement, 2);
		}, Main.mergeStatements, substring1);
		}
		else {
		}
		auto joinedNames = maybeNames.get().stream().map(auto _lambda29_(auto name){
			return "auto " + name;
		}).collect(Collectors.joining(", "));
		return Optional.of("auto " + getLambda__() + "(" + joinedNames + "){" + compiled + "\n\t\t}");
	}
	String getLambda__(){
		auto lambda = "_lambda" + counter + "_";counter++;
		return lambda;
	}
	Optional<List<String>> findLambdaNames(String nameSlice){
		if (nameSlice.isEmpty()) {
			return Optional.of(Collections.emptyList());
		}
		if (isSymbol(nameSlice)) {
			return Optional.of(List.of(nameSlice));
		}
		if (!nameSlice.startsWith("(") || !nameSlice.endsWith(")")) {
			return Optional.empty();
		}
		auto args = nameSlice.substring(1, nameSlice.length() - 1).split(", ");
		return Optional.of(Arrays.stream(args).map(String.strip).filter(auto _lambda30_(auto value){
			return !value.isEmpty();
		}).toList());
	}
	Optional<String> compileConstruction(int depth, String input){
		if (!input.startsWith("new ")) {
			return Optional.empty();
		}
		auto substring = input.substring("new ".length());
		if (!substring.endsWith(")")) {
			return Optional.empty();
		}
		auto substring2 = substring.substring(0, substring.length() - ")".length());
		return findArgStart(substring2).map(auto _lambda32_(auto index){
		auto caller = substring2.substring(0, index);
		auto compiled1 = compileType(caller.strip());
		auto substring1 = substring2.substring(index + 1);
		auto compiled = splitAndCompile(Main.splitByValues, auto _lambda31_(auto value){
			return compileValue(value.strip(), depth);
		}, Main.mergeValues, substring1);
		return compiled1 + "(" + compiled + ")";
		});
	}
	StringBuilder mergeValues(StringBuilder inner, String stripped){
		return inner.append(", ").append(stripped);
	}
	Optional<String> compileOperator(int depth, String input, String operator){
		auto index2 = input.indexOf(operator);
		if (index2 == -1) {
			return Optional.empty();
		}
		auto compiled = compileValue(input.substring(0, index2), depth);
		auto compiled1 = compileValue(input.substring(index2 + operator.length()), depth);
		return Optional.of(compiled + " " + operator + " " + compiled1);
	}
	boolean isNumber(String value){
		auto value1 = value.startsWith("-")
                ? value.substring(1)
                : value;
		for (;;) {
		}
		return true;
	}
	boolean isSymbol(String value){
		for (;;) {
		}
		return true;
	}
	String compileParams(ArrayList<String> inputParamsList){
		Optional<StringBuilder> maybeOutputParams = Optional.empty();
		for (;;) {
		}
		return maybeOutputParams.map(StringBuilder.toString).orElse("");
	}
	Optional<String> compileDefinition(String input){
		auto stripped = input.strip();
		auto separator = stripped.lastIndexOf(' ');
		if (separator == -1) {
			return Optional.empty();
		}
		auto inputParamType = stripped.substring(0, separator);
		auto paramName = stripped.substring(separator + 1);
		auto index = -1;
		auto depth = 0;
		for (;;) {
		}
		auto inputParamType1 = index == -1 ? inputParamType : inputParamType.substring(index + 1);
		auto outputParamType = compileType(inputParamType1);
		return Optional.of(outputParamType + " " + paramName);
	}
	String compileType(String input){
		if (input.equals("var")) {
			return "auto";
		}
		if (input.endsWith("[]")) {
			return "Slice<" + input.substring(0, input.length() - 2) + ">";
		}
		return compileGenericType(input).or(auto _lambda34_(){
			return compileSymbol(input);
		}).orElseGet(auto _lambda33_(){
			return invalidate("type", input);
		});
	}
	Optional<String> compileGenericType(String input){
		auto genStart = input.indexOf("<");
		if (genStart == -1) {
			return Optional.empty();
		}
		auto caller = input.substring(0, genStart);
		auto withEnd = input.substring(genStart + 1);
		if (!withEnd.endsWith(">")) {
			return Optional.empty();
		}
		auto inputArgs = withEnd.substring(0, withEnd.length() - ">".length());
		auto outputArgs = splitAndCompile(Main.splitByValues, Main.compileType, Main.mergeValues, inputArgs);
		return Optional.of(caller + "<" + outputArgs + ">");
	}
	ArrayList<String> splitByValues(String inputParams){
		auto inputParamsList = ArrayList<String>();
		auto buffer = StringBuilder();
		auto depth = 0;
		auto queue = IntStream.range(0, inputParams.length()).mapToObj(inputParams.charAt).collect(Collectors.toCollection(LinkedList.new));
		while (!queue.isEmpty()) {
			auto c = queue.pop();
			if (c == ',' && depth == 0) {
				advance(inputParamsList, buffer);
				buffer = StringBuilder();
			}
		else {
		}
		}
		advance(inputParamsList, buffer);
		return inputParamsList;
	}
}