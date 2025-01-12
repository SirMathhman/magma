import magma.collect.Deque;
import magma.collect.List;
import magma.collect.Set;
import magma.io.Error;
import magma.io.Path;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.JavaPaths;
import magma.java.JavaSet;
import magma.java.Strings;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.split.Splitter;
import magma.split.StatementSplitter;
import magma.split.ValueSplitter;
import magma.stream.ArrayHead;
import magma.stream.Collectors;
import magma.stream.HeadedStream;
import magma.stream.LengthHead;
import magma.stream.Stream;
import magma.stream.Streams;
import java.util.function.Function;
struct Main {
	Path SOURCE_DIRECTORY = JavaPaths.get(".", "src", "java");
	Path TARGET_DIRECTORY = JavaPaths.get(".", "src", "c");
	int counter = 0;
	void main(Slice<String> args){
		SOURCE_DIRECTORY.walk().mapValue(Main.filterPaths).match(Main.compileSources, Some.new).ifPresent(auto _lambda7_(Some[value=auto error]){
			return System.err.println(error.display());
		});
	}
	Option<Error> compileSources(Set<Path> sources){
		return sources.stream().map(Main.compileSource).flatMap(Streams.fromOption).next();
	}
	Option<Error> compileSource(Path source){
		auto relative = SOURCE_DIRECTORY.relativize(source);
		auto parent = relative.findParent().orElse(JavaPaths.get("."));
		auto namespace = computeNamespace(parent);
		auto name = computeName(relative);
		auto namespaceSlice = namespace.slice(0, 2).orElse(JavaList<>());
		if (namespaceSlice.equals(JavaList.of("magma", "java"))) {
			return None<>();
		}
		auto targetParent = namespace.stream().foldLeft(TARGET_DIRECTORY, Path.resolve);
		auto target = targetParent.resolve(name + ".c");
		return ensureDirectory(targetParent).or(auto _lambda8_(magma.option.None@2c13da15){
			return compileFromSourceToTarget(source, target);
		});
	}
	Option<Error> ensureDirectory(Path targetParent){
		if (targetParent.exists()) {
			return None<>();
		}
		return targetParent.createDirectories();
	}
	Option<Error> compileFromSourceToTarget(Path source, Path target){
		return source.readString().mapValue(Main.compile).match(target.writeString, Some.new);
	}
	String computeName(Path relative){
		auto name = relative.findFileName().toString();
		return name.substring(0, name.indexOf('.'));
	}
	List<String> computeNamespace(Path parent){
		return parent.streamNames().map(Path.toString).collect(JavaList.collector());
	}
	String compile(String root){
		return splitAndCompile(StatementSplitter(), Main.compileRootMember, root);
	}
	String splitAndCompile(Splitter splitter, Function<String, String> compiler, String input){
		return splitter.split(input).stream().map(String.strip).filter(auto _lambda10_(Some[value=auto value]){
			return !value.isEmpty();
		}).<Option<StringBuilder>>foldLeft(None<>(), auto _lambda9_(Some[value=auto output, auto stripped]){
			return compileAndMerge(splitter, compiler, output, stripped);
		}).map(StringBuilder.toString).orElse("");
	}
	Option<StringBuilder> compileAndMerge(Splitter splitter, Function<String, String> compiler, Option<StringBuilder> output, String stripped){
		auto compiled = compiler.apply(stripped);
		if (output.isEmpty()) {
			return Some<>(StringBuilder(compiled));
		}
		else {
			return output.map(auto _lambda11_(Some[value=auto inner]){
				return splitter.merge(inner, compiled);
			});
		}
	}
	String compileRootMember(String rootSegment){
		if (rootSegment.startsWith("package ")) {
			return "";
		}
		if (rootSegment.startsWith("import ")) {
			return rootSegment + "\n";
		}
		return compileToStruct("class", rootSegment).or(auto _lambda14_(magma.option.None@9e89d68){
			return compileToStruct("interface", rootSegment);
		}).or(auto _lambda13_(magma.option.None@368239c8){
			return compileToStruct("record", rootSegment);
		}).orElseGet(auto _lambda12_(magma.option.None@77556fd){
			return invalidate("root segment", rootSegment);
		});
	}
	Option<String> compileToStruct(String keyword, String rootSegment){
		auto classIndex = rootSegment.indexOf(keyword);
		if (classIndex ==  - 1) {
			return None<>();
		}
		auto withoutKeyword = rootSegment.substring(classIndex + keyword.length());
		auto contentStartIndex = withoutKeyword.indexOf("{");
		if (contentStartIndex ==  - 1) {
			return None<>();
		}
		auto name = withoutKeyword.substring(0, contentStartIndex).strip();
		auto content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
		auto compiled = splitAndCompile(StatementSplitter(), Main.compileClassSegment, content);
		return Some<>("struct " + name + " {" + compiled + "\n}");
	}
	String invalidate(String type, String rootSegment){
		System.err.println("Unknown " + type + ": " + rootSegment);
		return rootSegment;
	}
	String compileClassSegment(String classSegment){
		if (classSegment.endsWith(";")) {
			auto substring = classSegment.substring(0, classSegment.length() - 1);
			auto index = substring.indexOf('=');
			if (index !=  - 1) {
				auto definition = substring.substring(0, index);
				auto compiled = compileValue(2, substring.substring(index + 1));
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
							auto outputContent = splitAndCompile(StatementSplitter(), auto _lambda15_(Some[value=auto statement]){
								return compileStatement(statement, 2);
							}, inputContent);
							auto outputParams = splitAndCompile(ValueSplitter(), auto _lambda17_(Some[value=auto value]){
								return compileDefinition(value).orElseGet(auto _lambda16_(magma.option.None@3b192d32){
								return invalidate("definition", value);
							});
							}, inputParams);
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
				output = splitAndCompile(StatementSplitter(), auto _lambda18_(Some[value=auto statement0]){
					return compileStatement(statement0, depth + 1);
				}, substring1);
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
				auto compiled = compileDefinition(substring).or(auto _lambda20_(magma.option.None@311d617d){
					return compileSymbol(substring);
				}).orElseGet(auto _lambda19_(magma.option.None@16f65612){
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
		return compileDefinitionStatement(statement).or(auto _lambda23_(magma.option.None@2a33fae0){
			return compilePostfix(statement, "--", depth);
		}).or(auto _lambda22_(magma.option.None@ed17bee){
			return compilePostfix(statement, "++", depth);
		}).orElseGet(auto _lambda21_(magma.option.None@7c53a9eb){
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
                outputContent = splitAndCompile(new StatementSplitter(), statement1 -> compileStatement(statement1, depth + 1), substring);
            } else {
                outputContent = compileStatement(content, depth + 1);
            }

            final var indent = "\n" + "\t".repeat(depth);
            return new Some<>(indent + prefix + " (" + value + ") {" + outputContent + indent + "}");
        });
	}
	Option<Integer> findConditionParamEnd(String input){
		auto queue = streamCharsWithIndices(input).collect(JavaLinkedList.collector());
		auto depth = 0;
		while (!queue.isEmpty()) {
			auto popped = queue.popOrPanic();
			auto i = popped.left();
			auto c = popped.right();
			if (c == '\'') {
				auto popped1 = queue.popOrPanic().right();
				if (popped1 == '\\') {
					queue.popOrPanic();
				}
				queue.popOrPanic();
			}
			if (c == '"') {
				while (!queue.isEmpty()) {
					auto next = queue.popOrPanic().right();
					if (next == '"') {
						break;
					}
					if (next == '\\') {
						queue.popOrPanic();
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
		return findMatchingChar(substring, Main.streamReverseIndices, '(', ')', '(').map(auto _lambda25_(Some[value=auto index]){
		auto caller = substring.substring(0, index);
		auto substring1 = substring.substring(index + 1);
		auto compiled = splitAndCompile(ValueSplitter(), auto _lambda24_(Some[value=auto value]){
			return compileValue(depth, value.strip());
		}, substring1);
		auto newCaller = compileValue(depth, caller.strip());
		return newCaller + "(" + compiled + ")";
		});
	}
	Option<Integer> findMatchingChar(String input, Function<String, Stream<Integer>> streamer, char search, char enter, char exit){
		auto queue = streamer.apply(input).extendBy(input.charAt).collect(JavaLinkedList.collector());
		auto current = Tuple<Option<Integer>, Integer>(None<>(), 0);
		while (!queue.isEmpty()) {
			auto tuple = queue.popOrPanic();
			current = findArgStateFold(current, tuple, search, enter, exit, queue);
		}
		return current.left();
	}
	Stream<Integer> streamReverseIndices(String input){
		return HeadedStream<>(new LengthHead(input.length())).map(auto _lambda26_(Some[value=auto index]){
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
			auto popped = queue.popOrPanic();
			if (popped.right() == '\\') {
				queue.popOrPanic();
			}
			queue.popOrPanic();
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
		return compileSymbol(input).or(auto _lambda42_(magma.option.None@61064425){
			return compileNumber(input);
		}).or(auto _lambda41_(magma.option.None@e73f9ac){
			return compileString(input);
		}).or(auto _lambda40_(magma.option.None@1f89ab83){
			return compileChar(input);
		}).or(auto _lambda39_(magma.option.None@2437c6dc){
			return compileNot(depth, input);
		}).or(auto _lambda38_(magma.option.None@6ed3ef1){
			return compileConstruction(depth, input);
		}).or(auto _lambda37_(magma.option.None@71bc1ae4){
			return compileLambda(depth, input);
		}).or(auto _lambda36_(magma.option.None@39a054a5){
			return compileInvocation(depth, input);
		}).or(auto _lambda35_(magma.option.None@7a7b0070){
			return compileAccess(depth, input, ".");
		}).or(auto _lambda34_(magma.option.None@4cc77c2e){
			return compileAccess(depth, input, "::");
		}).or(auto _lambda33_(magma.option.None@12bb4df8){
			return compileOperator(depth, input, "+");
		}).or(auto _lambda32_(magma.option.None@77468bd9){
			return compileOperator(depth, input, "-");
		}).or(auto _lambda31_(magma.option.None@2f333739){
			return compileOperator(depth, input, "==");
		}).or(auto _lambda30_(magma.option.None@2aae9190){
			return compileOperator(depth, input, "!=");
		}).or(auto _lambda29_(magma.option.None@21588809){
			return compileOperator(depth, input, "&&");
		}).or(auto _lambda28_(magma.option.None@14899482){
			return compileTernary(depth, input);
		}).orElseGet(auto _lambda27_(magma.option.None@11028347){
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
			compiled = splitAndCompile(StatementSplitter(), auto _lambda43_(Some[value=auto statement]){
				return compileStatement(statement, depth);
			}, substring1);
		}
		else {
			compiled = generateReturn(compileValue(depth, afterArrow), depth + 1);
		}
		return maybeNames.map(auto _lambda45_(Some[value=auto names]){
		auto joinedNames = names.stream().map(auto _lambda44_(Some[value=auto name]){
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
			return Some<>(JavaList<>());
		}
		if (isSymbol(nameSlice)) {
			return Some<>(JavaList.of(nameSlice));
		}
		if (!nameSlice.startsWith("(") || !nameSlice.endsWith(")")) {
			return None<>();
		}
		auto args = nameSlice.substring(1, nameSlice.length() - 1).split(", ");
		return Some<>(HeadedStream<>(new ArrayHead<>(args))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .collect(JavaList.collector()));
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
		return findMatchingChar(withoutEnd, Main.streamReverseIndices, '(', ')', '(').map(auto _lambda47_(Some[value=auto index]){
		auto caller = withoutEnd.substring(0, index);
		auto compiled1 = compileType(caller.strip());
		auto substring1 = withoutEnd.substring(index + 1);
		auto compiled = splitAndCompile(ValueSplitter(), auto _lambda46_(Some[value=auto value]){
			return compileValue(depth, value.strip());
		}, substring1);
		return compiled1 + "(" + compiled + ")";
		});
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
		return Strings.streamChars(value1).collect(Collectors.allMatch(Character.isDigit));
	}
	boolean isSymbol(String value){
		return streamCharsWithIndices(value).collect(Collectors.allMatch(Main.isSymbolChar));
	}
	Character>> streamCharsWithIndices(String value){
		return HeadedStream<>(new LengthHead(value.length())).extendBy(value.charAt);
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
		auto inputParamType1 = findMatchingChar(inputParamType, Main.streamReverseIndices, ' ', '>', '<').map(auto _lambda48_(Some[value=auto index]){
			return inputParamType.substring(index + 1);
		}).orElse(inputParamType);
		auto outputParamType = compileType(inputParamType1);
		return Some<>(outputParamType + " " + paramName);
	}
	String compileType(String input){
		return compileVar(input).or(auto _lambda52_(magma.option.None@6bc168e5){
			return compileArray(input);
		}).or(auto _lambda51_(magma.option.None@383534aa){
			return compileGenericType(input);
		}).or(auto _lambda50_(magma.option.None@299a06ac){
			return compileSymbol(input);
		}).orElseGet(auto _lambda49_(magma.option.None@7b1d7fff){
			return invalidate("type", input);
		});
	}
	Option<String> compileVar(String input){
		return input.equals("var") ? new Some<>("auto") : new None<>();
	}
	Option<String> compileArray(String input){
		return truncateRight(input, "[]").map(auto _lambda53_(Some[value=auto inner]){
			return generateGeneric("Slice", compileType(inner));
		});
	}
	Option<String> compileGenericType(String input){
		return split(input, "<").flatMap(auto _lambda55_(Some[value=auto tuple]){
		auto caller = tuple.left();
		auto withEnd = tuple.right();
		return truncateRight(withEnd, ">").map(auto _lambda54_(Some[value=auto inputArgs]){
		auto outputArgs = splitAndCompile(ValueSplitter(), Main.compileType, inputArgs);
		return generateGeneric(caller, outputArgs);
		});
		});
	}
	String generateGeneric(String caller, String outputArgs){
		return caller + "<" + outputArgs + ">";
	}
	String>> split(String input, String infix){
		auto index = input.indexOf(infix);
		if (index ==  - 1) {
			return None<>();
		}
		auto left = input.substring(0, index);
		auto right = input.substring(index + infix.length());
		return Some<>(Tuple<>(left, right));
	}
	Option<String> truncateRight(String input, String suffix){
		return input.endsWith(suffix) ? new Some<>(input.substring(0, input.length() - suffix.length())) : new None<String>();
	}
	Set<Path> filterPaths(Set<Path> paths){
		return paths.stream().filter(Path.isRegularFile).filter(auto _lambda56_(Some[value=auto path]){
			return path.toString().endsWith(".java");
		}).collect(JavaSet.collector());
	}
}