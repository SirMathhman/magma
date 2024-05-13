package com.meti.compile;

import com.meti.result.Err;
import com.meti.result.Ok;
import com.meti.result.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.meti.compile.MagmaLang.renderDefinedFunction;
import static com.meti.result.Results.$Result;

public record MethodCompiler(String input) {
    static Optional<Result<ClassMemberResult, CompileException>> compileMethod(MethodCompiler methodCompiler) {
        var stripped = methodCompiler.input().strip();

        var paramStart = stripped.indexOf('(');
        if (paramStart == -1) return Optional.empty();

        var paramEnd = stripped.indexOf(')');
        if (paramEnd == -1) return Optional.empty();

        var paramString = stripped.substring(paramStart + 1, paramEnd);
        var renderedParams = new ParamsCompiler(paramString).compile();

        var before = stripped.substring(0, paramStart).strip();
        var separator = before.lastIndexOf(' ');

        var modifiersAndTypeString = before.substring(0, separator).strip();
        var name = before.substring(separator + 1).strip();

        var modifiersAndType = Strings.splitTypeString(modifiersAndTypeString);
        if (modifiersAndType.isEmpty()) return Optional.empty();

        var modifiers = modifiersAndType.subList(0, modifiersAndType.size() - 1);
        var inputType = modifiersAndType.get(modifiersAndType.size() - 1);

        var modifierString = modifiers.contains("private") ? "private " : "";

        var contentStart = stripped.indexOf("{");
        if (contentStart == -1) return Optional.empty();

        var contentEnd = stripped.lastIndexOf('}');
        if (contentEnd == -1) return Optional.empty();

        var content = stripped.substring(contentStart + 1, contentEnd);
        var inputContent = Strings.splitMembers(content);

        return Optional.of($Result(() -> {
            var outputType = compileType(inputType).$();
            var outputContent = compileMethodMembers(inputContent).$();

            var rendered = renderDefinedFunction(1, modifierString, name, renderedParams, ": " + outputType, outputContent);

            return modifiers.contains("static")
                    ? new ClassMemberResult(Collections.emptyList(), Collections.singletonList(rendered))
                    : new ClassMemberResult(Collections.singletonList(rendered), Collections.emptyList());
        }));
    }

    private static Result<String, CompileException> compileType(String inputType) {
        return compilePrimitiveType(inputType)
                .or(() -> compileSymbolType(inputType))
                .or(() -> compileGenericType(inputType))
                .orElseGet(() -> new Err<>(new CompileException("Unknown type: " + inputType)));
    }

    private static Optional<Result<String, CompileException>> compileGenericType(String inputType) {
        var genStart = inputType.indexOf('<');
        if (genStart == -1) return Optional.empty();

        var genEnd = inputType.lastIndexOf('>');
        if (genEnd == -1) return Optional.empty();

        var parent = inputType.substring(0, genStart);
        var childrenString = inputType.substring(genStart + 1, genEnd);
        var children = new ArrayList<String>();
        var builder = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < childrenString.length(); i++) {
            var c = childrenString.charAt(i);
            if (c == ',' && depth == 0) {
                children.add(builder.toString());
                builder = new StringBuilder();
            } else {
                if (c == '<') depth++;
                if (c == '>') depth--;
                builder.append(c);
            }
        }
        children.add(builder.toString());

        var newChildren = new ArrayList<String>();
        for (String child : children) {
            var stripped = child.strip();
            if (stripped.isEmpty()) continue;

            try {
                newChildren.add(compileType(stripped).$());
            } catch (CompileException e) {
                return Optional.of(new Err<>(new CompileException("Failed to compile generic type: " + inputType, e)));
            }
        }

        return Optional.of(new Ok<>(parent + "<" + String.join(", ", newChildren) + ">"));
    }

    private static Optional<Result<String, CompileException>> compileSymbolType(String inputType) {
        if (Strings.isSymbol(inputType)) {
            return Optional.of(new Ok<>(inputType));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Result<String, CompileException>> compilePrimitiveType(String inputType) {
        if (inputType.equals("void")) {
            return Optional.of(new Ok<>("Void"));
        }
        return Optional.empty();
    }

    private static Result<String, CompileException> compileMethodMembers(List<String> inputContent) {
        var outputContent = new StringBuilder();
        for (String inputMember : inputContent) {
            if (inputMember.isBlank()) continue;

            try {
                outputContent.append(new StatementCompiler(inputMember, 2).compile());
            } catch (CompileException e) {
                return new Err<>(e);
            }
        }

        return new Ok<>(outputContent.toString());
    }

}