#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
struct Main  {
	"src", "java");
	"src", "c");
	void main(void* __ref__, String[] args){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void collect(void* __ref__){
		struct Main  this = *(struct Main *) __ref__;
		return JavaFiles.walkSafe(SOURCE_DIRECTORY).mapValue(obj::property);
	}
	void filterPaths(void* __ref__, Set<Path> paths){
		struct Main  this = *(struct Main *) __ref__;
		return paths.stream()
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .collect(Collectors.toSet());
	}
	void runWithSources(void* __ref__, Set<Path> sourceSet){
		struct Main  this = *(struct Main *) __ref__;
		for(;;) {}
		return Optional.empty();
	}
	void runWithSource(void* __ref__, Path source, List<String> namespace, String name){
		struct Main  this = *(struct Main *) __ref__;
		final var targetParent = resolveTargetParent(namespace);
		if (1) {}
		return JavaFiles.readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> compileInputToTarget(input, targetParent, name))
                .match(obj::property);
	}
	void resolveTargetParent(void* __ref__, List<String> namespace){
		struct Main  this = *(struct Main *) __ref__;
		var targetParent = TARGET_DIRECTORY;
		for(;;) {}
		return targetParent;
	}
	void computeName(void* __ref__, Path relativized){
		struct Main  this = *(struct Main *) __ref__;
		final var name = relativized.getFileName().toString();
		final var separator = name.indexOf('.');
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
		final var target = targetParent.resolve(a + b.c");
		return JavaFiles.writeSafe(output, target)
                .map(JavaError::new)
                .map(obj::property);
	}
	void compileRoot(void* __ref__, String root){
		struct Main  this = *(struct Main *) __ref__;
		return compileSegments(obj::property);
	}
	void compileSegments(void* __ref__, String root, Function<String, Result<String, CompileError>> mapper){
		struct Main  this = *(struct Main *) __ref__;
		return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = temp();
	}
	void split(void* __ref__, String input){
		struct Main  this = *(struct Main *) __ref__;
		var state = temp();
		var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(obj::property));
		while (1) {}
		if (1) {}
		else {}
	}
	void splitAtChar(void* __ref__, State state, char c, Deque<Character> queue){
		struct Main  this = *(struct Main *) __ref__;
		final var appended = state.append(c);
		if (1) {}
		if (1) {}
		if (c = a && b.isLevel()) return appended.advance();
		if (c = a && b.isShallow()) return appended.exit().advance();
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
		final var keywordIndex = segment.indexOf(keyword);
		caller();
		final var contentStart = segment.indexOf(a + b.length());
		caller();
		final var contentEnd = segment.lastIndexOf('}');
		caller();
		final var maybeImplements = segment.substring(a + b.length(), contentStart);
	String name;
		caller();
		if (1) {}
		else {}
		final var content = segment.substring(a + b);
		caller();
		return Optional.of(outputResult.mapValue(a + b));
	}
	void compileStructMember(void* __ref__, String structMember, String name){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileDefinition(void* __ref__, String structMember){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var space = slice.lastIndexOf(' ');
		caller();
		caller();
		final var i = before.lastIndexOf(' ');
		final var type = before.substring(a + b);
		final var name = slice.substring(a + b);
		return Optional.of(temp());
	}
	void compileMethod(void* __ref__, String structName, String structMember){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var paramEnd = structMember.indexOf(')');
		caller();
		var params = Arrays.stream(structMember.substring(paramStart + 1, paramEnd).split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList();
		caller();
		final var i = before.lastIndexOf(' ');
		final var methodName = before.substring(a + b);
		final var contentStart = structMember.indexOf('{');
		caller();
		final var contentEnd = structMember.lastIndexOf('}');
		caller();
		var content = structMember.substring(a + b);
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
        }));
	}
	void compileStatement(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		List<Supplier<Optional<Result<String, CompileError>>>> list = List.of(a + b);
		var errors = temp();
		for(;;) {}
		return temp();
	}
	void compilePostfix(void* __ref__, String statement, String operator){
		struct Main  this = *(struct Main *) __ref__;
		return statement.endsWith(operator + ";") ? Optional.of(new Ok<>(statement)) : Optional.empty();
	}
	void compileFor(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Optional.empty();
	}
	void compileElse(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Optional.empty();
	}
	void compileConditional(void* __ref__, String prefix, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Optional.empty();
	}
	void compileInvocationStatement(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Optional.empty();
	}
	void compileReturn(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		caller();
		caller();
		caller();
	}
	void compileWhile(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Optional.empty();
	}
	void compileAssignment(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		caller();
		caller();
		final var destination = slice.substring(0, separator).strip();
		final var source = slice.substring(separator + 1).strip();
		return Optional.of(compileValue(source).mapValue(value -> "\n\t\t" + destination + " = a + b;
	}
	void compileValue(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileFunctionAccess(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		return value.contains("::") ? Optional.of(new Ok<>("obj::property")) : Optional.empty();
	}
	void compileOperator(void* __ref__, String value, String operator){
		struct Main  this = *(struct Main *) __ref__;
		return value.contains(operator) ? Optional.of(new Ok<>("a " + operator + " b")) : Optional.empty();
	}
	void compileConstruction(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		return value.startsWith("new ") ? Optional.of(new Ok<>("temp()")) : Optional.empty();
	}
	void compileChar(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Optional.empty();
	}
	void compileDataAccess(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		final var separator = value.indexOf('.');
		caller();
		caller();
		final var property = value.substring(a + b);
		return Optional.of(compileValue(object).mapValue(a + b." + property));
	}
	void compileInvocation(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var argumentsStart = findArgumentsStart(slice);
		caller();
		caller();
		final var compiled = compileValue(caller);
		final var substring = slice.substring(argumentsStart.get() + 1);
		final var result = compileValue(substring);
		caller();
	}
	void findArgumentsStart(void* __ref__, String slice){
		struct Main  this = *(struct Main *) __ref__;
	= 0;
		for(;;) {}
		return Optional.empty();
	}
	void compileSymbol(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		for(;;) {}
		return Optional.of(temp());
	}
	void compileImport(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		return segment.startsWith("import ") ? Optional.of(new Ok<>("#include \"temp.h\";\n")) : Optional.empty();
	}
	void compilePackage(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		return segment.startsWith("package ") ? Optional.of(new Ok<>("")) : Optional.empty();
	}
};