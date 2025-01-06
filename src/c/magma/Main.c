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
		return Node[value=Node[value=Node[value=JavaFiles].walkSafe(SOURCE_DIRECTORY).mapValue](Node[value=obj::property])];
	}
	void filterPaths(void* __ref__, Set<Path> paths){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=paths].stream()
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .collect](Node[value=Node[value=Node[value=Collectors].toSet](Node[value=])])];
	}
	void runWithSources(void* __ref__, Set<Path> sourceSet){
		struct Main  this = *(struct Main *) __ref__;
		for(;;) {}
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void runWithSource(void* __ref__, Path source, List<String> namespace, String name){
		struct Main  this = *(struct Main *) __ref__;
		final var targetParent = Node[value=Node[value=resolveTargetParent](Node[value=namespace])];
		if (1) {}
		return Node[value=Node[value=Node[value=JavaFiles].readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> compileInputToTarget(input, targetParent, name))
                .match](Node[value=obj::property])];
	}
	void resolveTargetParent(void* __ref__, List<String> namespace){
		struct Main  this = *(struct Main *) __ref__;
		var targetParent = Node[value=TARGET_DIRECTORY];
		for(;;) {}
		return Node[value=targetParent];
	}
	void computeName(void* __ref__, Path relativized){
		struct Main  this = *(struct Main *) __ref__;
		final var name = Node[value=Node[value=Node[value=relativized].getFileName().toString](Node[value=])];
		final var separator = Node[value=Node[value=Node[value=name].indexOf](Node[value=Node[value='].'])];
		caller();
	}
	void computeNamespace(void* __ref__, Path parent){
		struct Main  this = *(struct Main *) __ref__;
		final var namespace = Node[value=temp()];
		for(;;) {}
		return Node[value=namespace];
	}
	void compileInputToTarget(void* __ref__, String input, Path targetParent, String nameWithoutExt){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void writeOutputToTarget(void* __ref__, Path targetParent, String nameWithoutExt, String output){
		struct Main  this = *(struct Main *) __ref__;
		final var target = Node[value=Node[value=Node[value=targetParent].resolve](Node[value=Node[value=a + b].c"])];
		return Node[value=Node[value=Node[value=JavaFiles].writeSafe(output, target)
                .map(JavaError::new)
                .map](Node[value=obj::property])];
	}
	void compileRoot(void* __ref__, String root){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=compileSegments](Node[value=obj::property])];
	}
	void compileSegments(void* __ref__, String root, Function<String, Result<Node, CompileError>> mapper){
		struct Main  this = *(struct Main *) __ref__;
		return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = Node[value=temp()];
	}
	void split(void* __ref__, String input){
		struct Main  this = *(struct Main *) __ref__;
		var state = Node[value=temp()];
		var queue = Node[value=Node[value=Node[value=IntStream].range(0, input.length())
                .mapToObj(input::charAt)
                .collect](Node[value=Node[value=Node[value=Collectors].toCollection](Node[value=obj::property])])];
		while (1) {}
		if (1) {}
		else {}
	}
	void splitAtChar(void* __ref__, State state, char c, Deque<Character> queue){
		struct Main  this = *(struct Main *) __ref__;
		final var appended = Node[value=Node[value=Node[value=state].append](Node[value=c])];
		if (1) {}
		if (1) {}
		if (c = Node[value=Node[value=Node[value=a && b].isLevel()) return appended.advance](Node[value=])];
		if (c = Node[value=Node[value=Node[value=a && b].isShallow()) return appended.exit().advance](Node[value=])];
		caller();
		caller();
		return Node[value=appended];
	}
	void compileRootSegment(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileToStruct(void* __ref__, String segment, String keyword){
		struct Main  this = *(struct Main *) __ref__;
		final var keywordIndex = Node[value=Node[value=Node[value=segment].indexOf](Node[value=keyword])];
		caller();
		final var contentStart = Node[value=Node[value=Node[value=segment].indexOf](Node[value=Node[value=Node[value=a + b].length](Node[value=])])];
		caller();
		final var contentEnd = Node[value=Node[value=Node[value=segment].lastIndexOf](Node[value='}'])];
		caller();
		final var maybeImplements = Node[value=Node[value=Node[value=segment].substring](Node[value=Node[value=a + b].length(), contentStart])];
	String name;
		caller();
		if (1) {}
		else {}
		final var content = Node[value=Node[value=Node[value=segment].substring](Node[value=a + b])];
		caller();
		return Node[value=Node[value=Node[value=Optional].of](Node[value=Node[value=Node[value=outputResult].mapValue(output -> "struct " + name + " {" + output + "\n};").mapValue](Node[value=obj::property])])];
	}
	void compileStructMember(void* __ref__, String structMember, String name){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileDefinition(void* __ref__, String structMember){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var space = Node[value=Node[value=Node[value=slice].lastIndexOf](Node[value=' '])];
		caller();
		caller();
		final var i = Node[value=Node[value=Node[value=before].lastIndexOf](Node[value=' '])];
		final var type = Node[value=Node[value=Node[value=before].substring](Node[value=a + b])];
		final var name = Node[value=Node[value=Node[value=slice].substring](Node[value=a + b])];
		return Node[value=Node[value=Node[value=Optional].of](Node[value=temp()])];
	}
	void compileMethod(void* __ref__, String structName, String structMember){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var paramEnd = Node[value=Node[value=structMember].indexOf(')')];
		caller();
		var params = Node[value=Node[value=Node[value=Arrays].stream(structMember.substring(paramStart + 1, paramEnd).split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList](Node[value=])];
		caller();
		final var i = Node[value=Node[value=Node[value=before].lastIndexOf](Node[value=' '])];
		final var methodName = Node[value=Node[value=Node[value=before].substring](Node[value=a + b])];
		final var contentStart = Node[value=Node[value=Node[value=structMember].indexOf](Node[value='{'])];
		caller();
		final var contentEnd = Node[value=Node[value=Node[value=structMember].lastIndexOf](Node[value='}'])];
		caller();
		var content = Node[value=Node[value=Node[value=structMember].substring](Node[value=a + b])];
		return Optional.of(compileSegments(content, Main::compileStatement).mapValue(output -> {
            final String actualName;
            final List<String> outputParams;
            final String body;
            if (methodName.equals(structName)) {
                actualName = Node[value=Node[value=a + b].add("void* __ref__");
                copy.addAll(params);

                outputParams = copy;
                final var s = "struct " + structName;
                body = "\n\t\t" + s + " this = *(" + s + "*) __ref__;" + output;
            }

            return "\n\tvoid " + actualName + "(" + String.join(", ", outputParams) + "){" +
                    body +
                    "\n\t}";
        }).mapValue(Node::new))];
	}
	void compileStatement(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		List<Supplier<Optional<Result<Node, CompileError>>>> list = Node[value=Node[value=Node[value=List].of](Node[value=a + b])];
		var errors = Node[value=temp()];
		for(;;) {}
		return Node[value=temp()];
	}
	void compilePostfix(void* __ref__, String statement, String operator){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=statement].endsWith(operator + ";") ? Optional.of(new Ok<>(new Node(statement))) : Optional.empty](Node[value=])];
	}
	void compileFor(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void compileElse(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void compileConditional(void* __ref__, String prefix, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void compileInvocationStatement(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
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
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void compileAssignment(void* __ref__, String statement){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		caller();
		caller();
		final var destination = Node[value=Node[value=Node[value=slice].substring(0, separator).strip](Node[value=])];
		final var source = Node[value=Node[value=Node[value=slice].substring(separator + 1).strip](Node[value=])];
		return Optional.of(compileValue(source)
                .mapValue(value -> "\n\t\t" + destination + " = Node[value=Node[value=a + b].mapValue(Node::new))];
	}
	void compileValue(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
	}
	void compileFunctionAccess(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=value].contains("::") ? Optional.of(new Ok<>(new Node("obj::property"))) : Optional.empty](Node[value=])];
	}
	void compileOperator(void* __ref__, String value, String operator){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=value].contains(operator) ? Optional.of(new Ok<>(new Node("a " + operator + " b"))) : Optional.empty](Node[value=])];
	}
	void compileConstruction(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=value].startsWith("new ") ? Optional.of(new Ok<>(new Node("temp()"))) : Optional.empty](Node[value=])];
	}
	void compileChar(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void compileDataAccess(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		final var separator = Node[value=Node[value=Node[value=value].indexOf](Node[value=Node[value='].'])];
		caller();
		caller();
		final var property = Node[value=Node[value=Node[value=value].substring](Node[value=a + b])];
		caller();
	}
	void compileInvocation(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		caller();
		caller();
		final var argumentsStart = Node[value=Node[value=findArgumentsStart](Node[value=slice])];
		caller();
		caller();
		final var compiled = Node[value=Node[value=compileValue](Node[value=caller])];
		final var substring = Node[value=Node[value=Node[value=slice].substring](Node[value=Node[value=argumentsStart].get() + 1])];
		final var result = Node[value=Node[value=compileValue](Node[value=substring])];
		caller();
	}
	void findArgumentsStart(void* __ref__, String slice){
		struct Main  this = *(struct Main *) __ref__;
	= 0;
		for(;;) {}
		return Node[value=Node[value=Node[value=Optional].empty](Node[value=])];
	}
	void compileSymbol(void* __ref__, String value){
		struct Main  this = *(struct Main *) __ref__;
		for(;;) {}
		return Node[value=Node[value=Node[value=Optional].of](Node[value=temp()])];
	}
	void compileImport(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=segment].startsWith("import ") ? Optional.of(new Ok<>(new Node("#include \"temp.h\";\n"))) : Optional.empty](Node[value=])];
	}
	void compilePackage(void* __ref__, String segment){
		struct Main  this = *(struct Main *) __ref__;
		return Node[value=Node[value=Node[value=segment].startsWith("package ") ? Optional.of(new Ok<>(new Node(""))) : Optional.empty](Node[value=])];
	}
};