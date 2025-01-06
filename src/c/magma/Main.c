#include "jv/JavaFiles.h";
#include "magma/error/ApplicationError.h";
#include "magma/error/CompileError.h";
#include "magma/error/JavaError.h";
#include "magma/result/Err.h";
#include "magma/result/Ok.h";
#include "magma/result/Result.h";
#include "java/io/IOException.h";
#include "java/nio/file/Files.h";
#include "java/nio/file/Path.h";
#include "java/nio/file/Paths.h";
#include "java/util/ArrayList.h";
#include "java/util/Arrays.h";
#include "java/util/Deque.h";
#include "java/util/LinkedList.h";
#include "java/util/List.h";
#include "java/util/Optional.h";
#include "java/util/Set.h";
#include "java/util/function/Function.h";
#include "java/util/function/Supplier.h";
#include "java/util/stream/Collectors.h";
#include "java/util/stream/IntStream.h";
struct Main  {
	"src", "java");
	"src", "c");
	void main(void* __ref__, String[] args){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void collect(void* __ref__){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = JavaFiles.walkSafe(SOURCE_DIRECTORY).mapValue;
			__caller__(__caller__, obj::property)
		};
	}
	void filterPaths(void* __ref__, Set<Path> paths){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = paths.stream()
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .collect;
			__caller__(__caller__, {
			void __caller__ = Collectors.toSet;
			__caller__(__caller__)
		})
		};
	}
	void runWithSources(void* __ref__, Set<Path> sourceSet){
		struct Main  this = *(struct Main *) __ref__;
		for(;;) {}
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void runWithSource(void* __ref__, Path source, List<String> namespace, String name){
		struct Main  this = *(struct Main *) __ref__;
		final var targetParent = resolveTargetParent(namespace);
		if (1) {}
		return {
			void __caller__ = JavaFiles.readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> compileInputToTarget(input, targetParent, name))
                .match;
			__caller__(__caller__, obj::property)
		};
	}
	void resolveTargetParent(void* __ref__, List<String> namespace){
		struct Main  this = *(struct Main *) __ref__;
		var targetParent = TARGET_DIRECTORY;
		for(;;) {}
		return targetParent;
	}
	void computeName(void* __ref__, Path relativized){
		struct Main  this = *(struct Main *) __ref__;
		final var name = {
			void __caller__ = relativized.getFileName().toString;
			__caller__(__caller__)
		};
		final var separator = {
			void __caller__ = name.indexOf;
			__caller__(__caller__, '.')
		};
		caller();
	}
	void computeNamespace(void* __ref__, Path parent){
		struct Main  this = *(struct Main *) __ref__;
		final var namespace = temp();
		for(;;) {}
		return namespace;
	}
	void compileInputToTarget(void* __ref__, String input, Path targetParent, String nameWithoutExt){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void writeOutputToTarget(void* __ref__, Path targetParent, String nameWithoutExt, String output){
		struct Main  this = *(struct Main *) __ref__;
		final var target = {
			void __caller__ = targetParent.resolve;
			__caller__(__caller__, a + b.c")
		};
		return {
			void __caller__ = JavaFiles.writeSafe(output, target)
                .map(JavaError::new)
                .map;
			__caller__(__caller__, obj::property)
		};
	}
	void compileRoot(void* __ref__, String root){
		struct Main  this = *(struct Main *) __ref__;
		return compileSegments(obj::property);
	}
	void compileSegments(void* __ref__, String root, Function<String, Result<Node, CompileError>> mapper){
		struct Main  this = *(struct Main *) __ref__;
		return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = temp();
	}
	void split(void* __ref__, String input){
		struct Main  this = *(struct Main *) __ref__;
		var state = temp();
		var queue = {
			void __caller__ = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect;
			__caller__(__caller__, {
			void __caller__ = Collectors.toCollection;
			__caller__(__caller__, obj::property)
		})
		};
		while (1) {}
		if (1) {}
		else {}
	}
	void splitAtChar(void* __ref__, State state, char c, Deque<Character> queue){
		struct Main  this = *(struct Main *) __ref__;
		final var appended = {
			void __caller__ = state.append;
			__caller__(__caller__, c)
		};
		if (1) {}
		if (1) {}
		if (c = {
			void __caller__ = a && b.isLevel()) return appended.advance;
			__caller__(__caller__)
		};
		if (c = {
			void __caller__ = a && b.isShallow()) return appended.exit().advance;
			__caller__(__caller__)
		};
		caller();
		caller();
		return appended;
	}
	void compileRootSegment(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileToStruct(void* __ref__, String segment, String keyword){
		struct Main  this = *(struct Main *) __ref__;
		final var keywordIndex = {
			void __caller__ = segment.indexOf;
			__caller__(__caller__, keyword)
		};
		caller();
		final var contentStart = {
			void __caller__ = segment.indexOf;
			__caller__(__caller__, {
			void __caller__ = a + b.length;
			__caller__(__caller__)
		})
		};
		caller();
		final var contentEnd = {
			void __caller__ = segment.lastIndexOf;
			__caller__(__caller__, '}')
		};
		caller();
		final var maybeImplements = {
			void __caller__ = segment.substring;
			__caller__(__caller__, a + b.length(), contentStart)
		};
	String name;
		caller();
		if (1) {}
		else {}
		final var content = {
			void __caller__ = segment.substring;
			__caller__(__caller__, a + b)
		};
		caller();
		return {
			void __caller__ = Optional.of;
			__caller__(__caller__, {
			void __caller__ = outputResult.mapValue(output -> "struct " + name + " {" + output + "\n};").mapValue;
			__caller__(__caller__, obj::property)
		})
		};
	}
	void compileStructMember(void* __ref__, String structMember, String name){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileDefinition(void* __ref__, String structMember){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var space = {
			void __caller__ = slice.lastIndexOf;
			__caller__(__caller__, ' ')
		};
		caller();
		caller();
		final var i = {
			void __caller__ = before.lastIndexOf;
			__caller__(__caller__, ' ')
		};
		final var type = {
			void __caller__ = before.substring;
			__caller__(__caller__, a + b)
		};
		final var name = {
			void __caller__ = slice.substring;
			__caller__(__caller__, a + b)
		};
		return {
			void __caller__ = Optional.of;
			__caller__(__caller__, temp())
		};
	}
	void compileMethod(void* __ref__, String structName, String structMember){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var paramEnd = structMember.indexOf(')');
		caller();
		var params = {
			void __caller__ = Arrays.stream(structMember.substring(paramStart + 1, paramEnd).split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList;
			__caller__(__caller__)
		};
		caller();
		final var i = {
			void __caller__ = before.lastIndexOf;
			__caller__(__caller__, ' ')
		};
		final var methodName = {
			void __caller__ = before.substring;
			__caller__(__caller__, a + b)
		};
		final var contentStart = {
			void __caller__ = structMember.indexOf;
			__caller__(__caller__, '{')
		};
		caller();
		final var contentEnd = {
			void __caller__ = structMember.lastIndexOf;
			__caller__(__caller__, '}')
		};
		caller();
		var content = {
			void __caller__ = structMember.substring;
			__caller__(__caller__, a + b)
		};
		return Optional.of(compileSegments(content, Main::compileStatement).mapValue(output -> {
            final String actualName;
            final List<String> outputParams;
            final String body;
            if (methodName.equals(structName)) {
                actualName = a + b.add("void* __ref__");
                copy.addAll(params);

                outputParams = copy;
                final var s = "struct " + structName;
                body = "\n\t\t" + s + " this = *(" + s + "*) __ref__;" + output;
            }

            return "\n\tvoid " + actualName + "(" + String.join(", ", outputParams) + "){" +
                    body +
                    "\n\t}";
        }).mapValue(Node::new));
	}
	void compileStatement(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		List<Supplier<Optional<Result<Node, CompileError>>>> list = {
			void __caller__ = List.of;
			__caller__(__caller__, a + b)
		};
		var errors = temp();
		for(;;) {}
		return temp();
	}
	void compilePostfix(void* __ref__, String statement, String operator){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = statement.endsWith(operator + ";") ? Optional.of(new Ok<>(new Node(statement))) : Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileFor(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileElse(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileConditional(void* __ref__, String prefix, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileInvocationStatement(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileReturn(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		caller();
		caller();
		caller();
	}
	void compileAssignment(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		caller();
		caller();
		final var destination = {
			void __caller__ = slice.substring(0, separator).strip;
			__caller__(__caller__)
		};
		final var source = {
			void __caller__ = slice.substring(separator + 1).strip;
			__caller__(__caller__)
		};
		return Optional.of(compileValue(source)
                .mapValue(value -> "\n\t\t" + destination + " = a + b.value() + ";")
                .mapValue(Node::new));
	}
	void compileValue(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileFunctionAccess(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = value.contains("::") ? Optional.of(new Ok<>(new Node("obj::property"))) : Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileOperator(void* __ref__, String value, String operator){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = value.contains(operator) ? Optional.of(new Ok<>(new Node("a " + operator + " b"))) : Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileConstruction(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = value.startsWith("new ") ? Optional.of(new Ok<>(new Node("temp()"))) : Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileChar(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileDataAccess(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		final var separator = {
			void __caller__ = value.indexOf;
			__caller__(__caller__, '.')
		};
		caller();
		caller();
		final var property = {
			void __caller__ = value.substring;
			__caller__(__caller__, a + b)
		};
		caller();
	}
	void compileInvocation(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var argumentsStart = findArgumentsStart(slice);
		caller();
		caller();
		final var compiled = compileValue(caller);
		final var substring = {
			void __caller__ = slice.substring;
			__caller__(__caller__, argumentsStart.get() + 1)
		};
		final var result = compileValue(substring);
		return Optional.of(compiled.and(() -> result).mapValue(tuple -> {
            final var leftNode = tuple.left();
            final var leftValue = leftNode.value();
            final var rightValue = tuple.right().value();

            final String content;
            if (leftNode.is("data-access")) {
                final var arguments = rightValue.isEmpty() ? "__caller__" : "__caller__, " + rightValue;
                content = "{\n\t\t\tvoid __caller__ = " + leftValue + ";\n\t\t\t__caller__(" + arguments + ")\n\t\t}";
            } else {
                content = leftValue + "(" + rightValue + ")";
            }
            return new Node(content);
        }));
	}
	void findArgumentsStart(void* __ref__, String slice){
		struct Main  this = *(struct Main *) __ref__;
	= 0;
		for(;;) {}
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void compileSymbol(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		for(;;) {}
		return {
			void __caller__ = Optional.of;
			__caller__(__caller__, temp())
		};
	}
	void compileImport(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		caller();
		caller();
		return {
			void __caller__ = Optional.of;
			__caller__(__caller__, temp())
		};
	}
	void compilePackage(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		return {
			void __caller__ = segment.startsWith("package ") ? Optional.of(new Ok<>(new Node(""))) : Optional.empty;
			__caller__(__caller__)
		};
	}
};