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
	void main(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		caller();
	}
	void collect(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void filterPaths(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void runWithSources(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		for(;;) {}
		return value;
	}
	void runWithSource(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var targetParent = resolveTargetParent(namespace);
		if (1) {}
		return value;
	}
	void resolveTargetParent(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		var targetParent = TARGET_DIRECTORY;
		for(;;) {}
		return value;
	}
	void computeName(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var name = relativized.getFileName().toString();
		final var separator = name.indexOf('.');
		return value;
	}
	void computeNamespace(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var namespace = new ArrayList<String>();
		for(;;) {}
		return value;
	}
	void compileInputToTarget(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void writeOutputToTarget(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var target = targetParent.resolve(nameWithoutExt + ".c");
		return value;
	}
	void compileRoot(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void compileSegments(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
            for (String segment : segments) {
                final var stripped = segment.strip();
                if (!stripped.isEmpty()) {
                    output = output
                            .and(() -> mapper.apply(stripped))
                            .mapValue(tuple -> tuple.left().append(tuple.right()));
                }
            }

            return output.mapValue(StringBuilder::toString);
        });
	}
	void split(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		var state = new State();
		var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));
		while(1) {}
		if (1) {}
		else {}
	}
	void splitAtChar(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var appended = state.append(c);
		if (1) {}
		if (1) {}
		if (c = = ';' && appended.isLevel()) return appended.advance();
		if (c = = '}' && appended.isShallow()) return appended.exit().advance();
		if (c = = '{' || c == '(') return appended.enter();
		if (c = = '}' || c == ')') return appended.exit();
		return value;
	}
	void compileRootSegment(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void compileToStruct(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var keywordIndex = segment.indexOf(keyword);
		if (keywordIndex = = -1) return Optional.empty();
		final var contentStart = segment.indexOf('{', keywordIndex + keyword.length());
		if (contentStart = = -1) return Optional.empty();
		final var contentEnd = segment.lastIndexOf('}');
		if (contentEnd = = -1) return Optional.empty();
		final var maybeImplements = segment.substring(keywordIndex + keyword.length(), contentStart);
		int value
		final var implementsIndex = maybeImplements.indexOf("implements ");
		if (1) {}
		else {}
		final var content = segment.substring(contentStart + 1, contentEnd);
		final var outputResult = compileSegments(content, structMember -> compileStructMember(structMember, name));
		return value;
	}
	void compileStructMember(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void compileDefinition(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		caller();
		final var slice = structMember.substring(0, structMember.length() - 1);
		final var space = slice.lastIndexOf(' ');
		if (space = = -1) return Optional.empty();
		final var before = slice.substring(0, space);
		final var i = before.lastIndexOf(' ');
		final var type = before.substring(i + 1);
		final var name = slice.substring(space + 1);
		return value;
	}
	void compileMethod(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var paramStart = structMember.indexOf("(");
		if (paramStart = = -1) return Optional.empty();
		final var before = structMember.substring(0, paramStart);
		final var i = before.lastIndexOf(' ');
		final var methodName = before.substring(i + 1);
		final var contentStart = structMember.indexOf('{');
		if (contentStart = = -1) return Optional.empty();
		final var contentEnd = structMember.lastIndexOf('}');
		if (contentEnd = = -1) return Optional.empty();
		var content = structMember.substring(contentStart + 1, contentEnd);
		return Optional.of(compileSegments(content, Main::compileStatement).mapValue(output -> {
            final String actualName;
            final String params;
            final String body;
            if (methodName.equals(structName)) {
                actualName = "new";
                params = "";
                body = "\n\t\tstruct " + structName + " this;" +
                        output +
                        "\n\t\treturn this;";
            } else {
                actualName = methodName;
                params = "void* __ref__";
                final var s = "struct " + structName;
                body = "\n\t\t" + s + "* this = (" + s + "*) __ref__;" + output;
            }

            return "\n\tvoid " + actualName + "(" + params + "){" +
                    body +
                    "\n\t}";
        }));
	}
	void compileStatement(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		final var x = compileAssignment(statement);
		caller();
		caller();
		caller();
		caller();
		caller();
		caller();
		caller();
		caller();
		return value;
	}
	void compileAssignment(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		caller();
		final var slice = statement.substring(0, statement.length() - ";".length());
		final var separator = slice.indexOf("=");
		if (separator = = -1) return Optional.empty();
		final var destination = slice.substring(0, separator).strip();
		final var source = slice.substring(separator + 1).strip();
		return Optional.of(new Ok<>("\n\t\t" + destination + " = " + source + ";"));
	}
	void compileImport(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
	void compilePackage(void* __ref__){
		struct Main * this = (struct Main *) __ref__;
		return value;
	}
};