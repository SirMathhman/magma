import magma.java.JavaPaths;
import magma.java.JavaSet;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.HeadedStream;
import magma.stream.LengthHead;
import magma.stream.Stream;
import magma.stream.Streams;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
struct Main {
	Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
	Path TARGET_DIRECTORY = Paths.get(".", "src", "c");
	int counter = 0;
	void main(Slice<String> args){
		JavaPaths.collect().mapValue(JavaSet.new).match(Main.compileSources, Some.new).ifPresent(Throwable.printStackTrace);
	}
	Option<IOException> compileSources(JavaSet<Path> sources){
		return sources.stream().map(Main.compileSource).flatMap(Streams.from).next();
	}
	Option<IOException> compileSource(Path source){
		auto relative = SOURCE_DIRECTORY.relativize(source);
		auto parent = relative.getParent();
		auto namespace = computeNamespace(parent);
		auto name = computeName(relative);
		if (namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))) {
			return None<>();
		}
		auto targetParent = namespace.stream().reduce(TARGET_DIRECTORY, Path.resolve, auto _lambda0_(auto _, auto next){
			return next;
		});
		auto target = targetParent.resolve(name + ".c");
		return ensureDirectory(targetParent).or(auto _lambda1_(){
			return compileFromSourceToTarget(source, target);
		});
	}
	Option<IOException> ensureDirectory(Path targetParent){
		if (Files.exists(targetParent)) {
			return None<>();
		}
		return JavaPaths.createDirectoriesSafe(targetParent);
	}
	Option<IOException> compileFromSourceToTarget(Path source, Path target){
		return JavaPaths.readSafe(source).mapValue(Main.compile).match(auto _lambda2_(auto output){
			return JavaPaths.writeSafe(target, output);
		}, Some.new);
	}
	String computeName(Path relative){
		auto name = relative.getFileName().toString();
		return name.substring(0, name.indexOf('.'));
	}
	List<String> computeNamespace(Path parent){
		return IntStream.range(0, parent.getNameCount()).mapToObj(parent.getName).map(Path.toString).toList();
	}
	String compile(String root){
		return splitAndCompile(Main.splitByStatements, auto _lambda3_(auto rootSegment){
			return compileRootMember(rootSegment, 1);
		}, Main.mergeStatements, root);
	}
	String splitAndCompile(Function<String, List<String>> splitter, Function<String, String> compiler, BiFunction<StringBuilder, String, StringBuilder> merger, String input){
		return splitter.apply(input).stream().map(String.strip).filter(auto _lambda6_(auto value){
			return !value.isEmpty();
		}).<Option<StringBuilder>>reduce(None<>(), auto _lambda4_(auto output, auto stripped){
			return compileAndMerge(compiler, merger, output, stripped);
		}, auto _lambda5_(auto _, auto next){
			return next;
		}).map(StringBuilder.toString).orElse("");
	}
	Option<StringBuilder> compileAndMerge(Function<String, String> compiler, BiFunction<StringBuilder, String, StringBuilder> merger, Option<StringBuilder> output, String stripped){
		auto compiled = compiler.apply(stripped);
		if (output.isEmpty()) {
			return Some<>(StringBuilder(compiled));
		}
		else {
			return output.map(auto _lambda7_(auto inner){
				return merger.apply(inner, compiled);
			});
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
				if (c == '}' && depth == 1) {
					depth--;
					advance(segments, buffer);
					buffer = StringBuilder();
				}
			}
			else {
				if (c == '{' || c == '(') {
					depth++;
				}
				if (c == '}' || c == ')') {
					depth--;
				}
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
	String compileRootMember(String rootSegment, int depth){
		if (rootSegment.startsWith("package ")) {
			return "";
		}
		if (rootSegment.startsWith("import ")) {
			return rootSegment + "\n";
		}
		auto classIndex = rootSegment.indexOf("class");
		if (classIndex !=  - 1) {
			auto withoutKeyword = rootSegment.substring(classIndex + "class".length());
			auto contentStartIndex = withoutKeyword.indexOf("{");
			if (contentStartIndex !=  - 1) {
				auto name = withoutKeyword.substring(0, contentStartIndex).strip();
				auto content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
				auto compiled = splitAndCompile(Main.splitByStatements, auto _lambda8_(auto classSegment){
					return compileClassSegment(classSegment, depth + 1);
				}, Main.mergeStatements, content);
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
	String compileClassSegment(String classSegment, int depth){
		if (classSegment.endsWith(";")) {
			auto substring = classSegment.substring(0, classSegment.length() - 1);
			auto index = substring.indexOf('=');
			if (index !=  - 1) {
				auto definition = substring.substring(0, index);
				auto compiled = compileValue(depth, substring.substring(index + 1));
				return "\n\t" + compileDefinition(definition).orElseGet(() -> invalidate("definition", definition)) + " = " + compiled + ";";
			}
		}
		auto paramStart = classSegment.indexOf('(');
		if (paramStart !=  - 1) {
			auto beforeParamStart = classSegment.substring(0, paramStart);
			auto afterParamStart = classSegment.substring(paramStart + 1);
			auto paramEnd = afterParamStart.indexOf(')');
			if (paramEnd !=  - 1) {
				auto nameSeparator = beforeParamStart.lastIndexOf(' ');
				if (nameSeparator !=  - 1) {
					auto beforeName = beforeParamStart.substring(0, nameSeparator);
					auto typeSeparator = beforeName.lastIndexOf(' ');
					if (typeSeparator !=  - 1) {
						auto type = beforeName.substring(typeSeparator + 1);
						auto name = beforeParamStart.substring(nameSeparator + 1);
						auto inputParams = afterParamStart.substring(0, paramEnd);
						auto afterParams = afterParamStart.substring(paramEnd + 1).strip();
						if (afterParams.startsWith("{") && afterParams.endsWith("}")) {
							auto inputContent = afterParams.substring(1, afterParams.length() - 1);
							auto outputContent = splitAndCompile(Main.splitByStatements, auto _lambda9_(auto statement){
								return compileStatement(statement, depth);
							}, Main.mergeStatements, inputContent);
							auto outputParams = splitAndCompile(Main.splitByValues, auto _lambda11_(auto value){
								return compileDefinition(value).orElseGet(auto _lambda10_(){
								return invalidate("definition", value);
							});
							}, Main.mergeValues, inputParams);
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
		if (statement.startsWith("else")) {
			auto substring = statement.substring("else".length()).strip();String output
			if (substring.startsWith("{") && substring.endsWith("}")) {
				auto substring1 = substring.substring(1, substring.length() - 1);
				output = splitAndCompile(Main.splitByStatements, auto _lambda12_(auto statement0){
					return compileStatement(statement0, depth + 1);
				}, Main.mergeStatements, substring1);
			}
			else {
				output = compileStatement(substring, depth + 1);
			}
			auto indent = "\n" + "\t".repeat(depth);
			return indent + "else {" + output + indent + "}";
		}
		if (statement.startsWith("return ")) {
			auto substring = statement.substring("return ".length());
			if (substring.endsWith(";")) {
				auto substring1 = substring.substring(0, substring.length() - ";".length());
				auto compiled = compileValue(depth, substring1);
				return generateReturn(compiled, depth);
			}
		}
		auto Option1 = compileConditional(depth, "while", statement);
		if (Option1.isPresent()) {
			return Option1.unwrap();
		}
		auto value = compileConditional(depth, "if", statement);
		if (value.isPresent()) {
			return value.unwrap();
		}
		auto index1 = statement.indexOf("=");
		if (index1 !=  - 1) {
			auto substring = statement.substring(0, index1);
			auto substring1 = statement.substring(index1 + 1);
			if (substring1.endsWith(";")) {
				auto compiled = compileDefinition(substring).or(auto _lambda14_(){
					return compileSymbol(substring);
				}).orElseGet(auto _lambda13_(){
					return invalidate("definition", substring);
				});
				auto compiled1 = compileValue(depth, substring1.substring(0, substring1.length() - ";".length()).strip());
				return generateStatement(depth, compiled + " = " + compiled1);
			}
		}
		if (statement.endsWith(";")) {
			auto newCaller = compileInvocation(depth, statement.substring(0, statement.length() - ";".length()));
			if (newCaller.isPresent()) {
				return generateStatement(depth, newCaller.unwrap());
			}
		}
		return compileDefinitionStatement(statement).or(auto _lambda17_(){
			return compilePostfix(statement, "--", depth);
		}).or(auto _lambda16_(){
			return compilePostfix(statement, "++", depth);
		}).orElseGet(auto _lambda15_(){
			return invalidate("statement", statement);
		});
	}
	Option<String> compileDefinitionStatement(String statement){
		if (!statement.endsWith(";")) {
			return None<>();
		}
		return compileDefinition(statement.substring(0, statement.length() - 1));
	}
	Option<String> compilePostfix(String statement, String suffix, int depth){
		auto joined = suffix + ";";
		if (!statement.endsWith(joined)) {
			return None<>();
		}
		auto substring = statement.substring(0, statement.length() -(joined).length());
		return Some<>(generateStatement(depth, compileValue(depth, substring) + suffix));
	}
	String generateStatement(int depth, String content){
		return "\n" + "\t".repeat(depth) + content + ";";
	}
	String generateReturn(String compiled, int depth){
		return "\n" + "\t".repeat(depth) + "return " + compiled + ";";
	}
	Option<String> compileConditional(int depth, String prefix, String statement){
		if (!statement.startsWith(prefix)) {
			return None<>();
		}
		auto withoutKeyword = statement.substring(prefix.length());
		return findConditionParamEnd(withoutKeyword).flatMap(paramEnd -> {
            final var conditionWithEnd = withoutKeyword.substring(0, paramEnd).strip();
            final var content = withoutKeyword.substring(paramEnd + 1).strip();

            if(!conditionWithEnd.startsWith("(")) return new None<>();

            final var condition = conditionWithEnd.substring(1);
            final var value = compileValue(depth, condition);
            final String outputContent;
            if (content.startsWith("{") && content.endsWith("}")) {
                final var substring = content.substring(1, content.length() - 1);
                outputContent = splitAndCompile(Main::splitByStatements, statement1 -> compileStatement(statement1, depth + 1), Main::mergeStatements, substring);
            } else {
                outputContent = compileStatement(content, depth + 1);
            }

            final var indent = "\n" + "\t".repeat(depth);
            return new Some<>(indent + prefix + " (" + value + ") {" + outputContent + indent + "}");
        });
	}
	Option<Integer> findConditionParamEnd(String input){
		auto queue = IntStream.range(0, input.length()).mapToObj(auto _lambda18_(auto index){
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
				return Some<>(i);
			}
			else {
				if (c == '(') {
					depth++;
				}
				if (c == ')') {
					depth--;
				}
			}
		}
		return None<>();
	}
	Option<String> compileInvocation(int depth, String statement){
		auto stripped = statement.strip();
		if (!stripped.endsWith(")")) {
			return None<>();
		}
		auto substring = stripped.substring(0, stripped.length() - ")".length());
		return findMatchingChar(substring, Main.streamReverseIndices, '(', ')', '(').map(auto _lambda20_(auto index){
		auto caller = substring.substring(0, index);
		auto substring1 = substring.substring(index + 1);
		auto compiled = splitAndCompile(Main.splitByValues, auto _lambda19_(auto value){
			return compileValue(depth, value.strip());
		}, Main.mergeValues, substring1);
		auto newCaller = compileValue(depth, caller.strip());
		return newCaller + "(" + compiled + ")";
		});
	}
	Option<Integer> findMatchingChar(String input, Function<String, Stream<Integer>> streamer, char search, char enter, char exit){
		auto queue = streamer.apply(input).map(auto _lambda22_(auto index){
			return Tuple<>(index, input.charAt(index));
		}).foldLeft(LinkedList<Tuple<Integer, Character>>(), auto _lambda21_(auto tuples, auto tuple){
		tuples.add(tuple);
		return tuples;
		});
		auto current = Tuple<Option<Integer>, Integer>(None<>(), 0);
		while (!queue.isEmpty()) {
			auto tuple = queue.pop();
			current = findArgStateFold(current, tuple, search, enter, exit, queue);
		}
		return current.left();
	}
	Stream<Integer> streamReverseIndices(String input){
		return HeadedStream<>(new LengthHead(input.length())).map(auto _lambda23_(auto index){
			return input.length() - 1 - index;
		});
	}
	Integer> findArgStateFold(Tuple<Option<Integer>, Integer> previous, Tuple<Integer, Character> tuple, char search, char enter, char exit, Deque<Tuple<Integer, Character>> queue){
		auto previousOption = previous.left();
		if (previousOption.isPresent()) {
			return previous;
		}
		auto depth = previous.right();
		auto i = tuple.left();
		auto c = tuple.right();
		if (c == '\'') {
			auto popped = queue.pop();
			if (popped.right() == '\\') {
				queue.pop();
			}
			queue.pop();
		}
		if (c == search && depth == 0) {
			return Tuple<>(Some<>(i), depth);
		}
		if (c == enter) {
			return Tuple<>(None<>(), depth + 1);
		}
		if (c == exit) {
			return Tuple<>(None<>(), depth - 1);
		}
		return Tuple<>(None<>(), depth);
	}
	String compileValue(int depth, String input){
		return compileSymbol(input).or(auto _lambda39_(){
			return compileNumber(input);
		}).or(auto _lambda38_(){
			return compileString(input);
		}).or(auto _lambda37_(){
			return compileChar(input);
		}).or(auto _lambda36_(){
			return compileNot(depth, input);
		}).or(auto _lambda35_(){
			return compileConstruction(depth, input);
		}).or(auto _lambda34_(){
			return compileLambda(depth, input);
		}).or(auto _lambda33_(){
			return compileInvocation(depth, input);
		}).or(auto _lambda32_(){
			return compileAccess(depth, input, ".");
		}).or(auto _lambda31_(){
			return compileAccess(depth, input, "::");
		}).or(auto _lambda30_(){
			return compileOperator(depth, input, "+");
		}).or(auto _lambda29_(){
			return compileOperator(depth, input, "-");
		}).or(auto _lambda28_(){
			return compileOperator(depth, input, "==");
		}).or(auto _lambda27_(){
			return compileOperator(depth, input, "!=");
		}).or(auto _lambda26_(){
			return compileOperator(depth, input, "&&");
		}).or(auto _lambda25_(){
			return compileTernary(depth, input);
		}).orElseGet(auto _lambda24_(){
			return invalidate("value", input);
		});
	}
	Option<String> compileNumber(String input){
		auto stripped = input.strip();
		if (isNumber(stripped)) {
			return Some<>(stripped);
		}
		return None<>();
	}
	Option<String> compileNot(int depth, String input){
		if (input.startsWith("!")) {
			return Some<>("!" + compileValue(depth, input.substring(1)));
		}
		return None<>();
	}
	Option<String> compileString(String input){
		auto stripped = input.strip();
		if (stripped.startsWith("\"") && stripped.endsWith("\"")) {
			return Some<>(stripped);
		}
		return None<>();
	}
	Option<String> compileChar(String input){
		auto stripped = input.strip();
		if (stripped.startsWith("'") && stripped.endsWith("'")) {
			return Some<>(stripped);
		}
		return None<>();
	}
	Option<String> compileAccess(int depth, String input, String slice){
		auto index = input.lastIndexOf(slice);
		if (index ==  - 1) {
			return None<>();
		}
		auto substring = input.substring(0, index);
		auto substring1 = input.substring(index + slice.length());
		auto s = compileValue(depth, substring);
		return Some<>(generateDataAccess(s, substring1));
	}
	String generateDataAccess(String s, String substring1){
		return s + "." + substring1;
	}
	Option<String> compileTernary(int depth, String stripped){
		auto index3 = stripped.indexOf('?');
		if (index3 ==  - 1) {
			return None<>();
		}
		auto condition = stripped.substring(0, index3);
		auto substring = stripped.substring(index3 + 1);
		auto maybe = substring.indexOf(':');
		if (maybe ==  - 1) {
			return None<>();
		}
		auto ifTrue = substring.substring(0, maybe);
		auto ifFalse = substring.substring(maybe + 1);
		return Some<>(compileValue(depth, condition) + " ? " + compileValue(depth, ifTrue) + " : " + compileValue(depth, ifFalse));
	}
	Option<String> compileSymbol(String input){
		auto stripped = input.strip();
		if (isSymbol(stripped)) {
			return Some<>(stripped);
		}
		return None<>();
	}
	Option<String> compileLambda(int depth, String input){
		auto arrowIndex = input.indexOf("->");
		if (arrowIndex ==  - 1) {
			return None<>();
		}
		auto beforeArrow = input.substring(0, arrowIndex).strip();
		auto afterArrow = input.substring(arrowIndex + "->".length()).strip();
		auto maybeNames = findLambdaNames(beforeArrow);
		if (maybeNames.isEmpty()) {
			return None<>();
		}String compiled
		if (afterArrow.startsWith("{") && afterArrow.endsWith("}")) {
			auto substring1 = afterArrow.substring(1, afterArrow.length() - 1);
			compiled = splitAndCompile(Main.splitByStatements, auto _lambda40_(auto statement){
				return compileStatement(statement, depth);
			}, Main.mergeStatements, substring1);
		}
		else {
			compiled = generateReturn(compileValue(depth, afterArrow), depth + 1);
		}
		return maybeNames.map(auto _lambda42_(auto names){
		auto joinedNames = names.stream().map(auto _lambda41_(auto name){
			return "auto " + name;
		}).collect(Collectors.joining(", "));
		return "auto " + createUniqueName() + "(" + joinedNames + "){" + compiled + "\n" + "\t".repeat(depth) + "}";
		});
	}
	String createUniqueName(){
		auto lambda = "_lambda" + counter + "_";
		counter++;
		return lambda;
	}
	Option<List<String>> findLambdaNames(String nameSlice){
		if (nameSlice.isEmpty()) {
			return Some<>(Collections.emptyList());
		}
		if (isSymbol(nameSlice)) {
			return Some<>(List.of(nameSlice));
		}
		if (!nameSlice.startsWith("(") || !nameSlice.endsWith(")")) {
			return None<>();
		}
		auto args = nameSlice.substring(1, nameSlice.length() - 1).split(", ");
		return Some<>(Arrays.stream(args).map(String.strip).filter(auto _lambda43_(auto value){
			return !value.isEmpty();
		}).toList());
	}
	Option<String> compileConstruction(int depth, String input){
		if (!input.startsWith("new ")) {
			return None<>();
		}
		auto substring = input.substring("new ".length());
		if (!substring.endsWith(")")) {
			return None<>();
		}
		auto withoutEnd = substring.substring(0, substring.length() - ")".length());
		return findMatchingChar(withoutEnd, Main.streamReverseIndices, '(', ')', '(').map(auto _lambda45_(auto index){
		auto caller = withoutEnd.substring(0, index);
		auto compiled1 = compileType(caller.strip());
		auto substring1 = withoutEnd.substring(index + 1);
		auto compiled = splitAndCompile(Main.splitByValues, auto _lambda44_(auto value){
			return compileValue(depth, value.strip());
		}, Main.mergeValues, substring1);
		return compiled1 + "(" + compiled + ")";
		});
	}
	StringBuilder mergeValues(StringBuilder inner, String stripped){
		return inner.append(", ").append(stripped);
	}
	Option<String> compileOperator(int depth, String input, String operator){
		auto index2 = input.indexOf(operator);
		if (index2 ==  - 1) {
			return None<>();
		}
		auto compiled = compileValue(depth, input.substring(0, index2));
		auto compiled1 = compileValue(depth, input.substring(index2 + operator.length()));
		return Some<>(compiled + " " + operator + " " + compiled1);
	}
	boolean isNumber(String value){
		auto value1 = value.startsWith("-")
                ? value.substring(1)
                : value;
		return IntStream.range(0, value1.length()).mapToObj(value1.charAt).allMatch(Character.isDigit);
	}
	boolean isSymbol(String value){
		return IntStream.range(0, value.length()).mapToObj(auto _lambda46_(auto index){
			return Tuple<>(index, value.charAt(index));
		}).allMatch(Main.isSymbolChar);
	}
	boolean isSymbolChar(Tuple<Integer, Character> tuple){
		auto i = tuple.left();
		auto c = tuple.right();
		return Character.isLetter(c) || c == '_' ||(i != 0 && Character.isDigit(c));
	}
	Option<String> compileDefinition(String input){
		auto stripped = input.strip();
		auto separator = stripped.lastIndexOf(' ');
		if (separator ==  - 1) {
			return None<>();
		}
		auto inputParamType = stripped.substring(0, separator);
		auto paramName = stripped.substring(separator + 1);
		auto inputParamType1 = findMatchingChar(inputParamType, Main.streamReverseIndices, ' ', '>', '<').map(auto _lambda47_(auto index){
			return inputParamType.substring(index + 1);
		}).orElse(inputParamType);
		auto outputParamType = compileType(inputParamType1);
		return Some<>(outputParamType + " " + paramName);
	}
	String compileType(String input){
		if (input.equals("var")) {
			return "auto";
		}
		if (input.endsWith("[]")) {
			return "Slice<" + input.substring(0, input.length() - "[]".length()) + ">";
		}
		return compileGenericType(input).or(auto _lambda49_(){
			return compileSymbol(input);
		}).orElseGet(auto _lambda48_(){
			return invalidate("type", input);
		});
	}
	Option<String> compileGenericType(String input){
		auto genStart = input.indexOf("<");
		if (genStart ==  - 1) {
			return None<>();
		}
		auto caller = input.substring(0, genStart);
		auto withEnd = input.substring(genStart + 1);
		if (!withEnd.endsWith(">")) {
			return None<>();
		}
		auto inputArgs = withEnd.substring(0, withEnd.length() - ">".length());
		auto outputArgs = splitAndCompile(Main.splitByValues, Main.compileType, Main.mergeValues, inputArgs);
		return Some<>(caller + "<" + outputArgs + ">");
	}
	List<String> splitByValues(String inputParams){
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
				buffer.append(c);
				if (c == ' - ') {
					if (!queue.isEmpty() && queue.peek() == '>') {
						buffer.append(queue.pop());
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
		advance(inputParamsList, buffer);
		return inputParamsList;
	}
}