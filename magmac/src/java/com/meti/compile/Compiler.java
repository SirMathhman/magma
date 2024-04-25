package com.meti.compile;

import com.meti.collect.JavaString;
import com.meti.collect.Range;
import com.meti.option.Option;
import com.meti.option.ThrowableOption;
import com.meti.result.Result;

import java.util.*;
import java.util.stream.Collectors;

public class Compiler {
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";
    public static final String EXPORT_KEYWORD_WITH_SPACE = "export ";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String PUBLIC_KEYWORD_WITH_SPACE = PUBLIC_KEYWORD + " ";
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";
    public static final String STATIC_KEYWORD = "static";
    public static final String STATIC_KEYWORD_WITH_SPACE = STATIC_KEYWORD + " ";
    public static final char BLOCK_START = '{';
    public static final char BLOCK_END = '}';
    public static final String INT_KEYWORD = "int";
    public static final String I32_KEYWORD = "I32";
    public static final String LONG_KEYWORD = "long";
    public static final String I64_KEYWORD = "I64";
    public static final String VALUE_SEPARATOR = "=";
    public static final String FINAL_KEYWORD = "final";
    public static final String FINAL_KEYWORD_WITH_SPACE = FINAL_KEYWORD + " ";
    public static final String LET_KEYWORD_WITH_SPACE = "let ";
    public static final String CONST_KEYWORD_WITH_SPACE = "const ";

    public static String renderJavaDefinition(String type, String name, String value) {
        return renderJavaDefinition("", type, name, value);
    }

    public static String renderJavaDefinition(String modifiersString, String type, String name, String value) {
        return modifiersString + type + " " + name + " " + VALUE_SEPARATOR + " " + value + ";";
    }

    public static String renderMagmaDefinitionUnsafe(String name, String type, String value) {
        return renderMagmaDefinitionUnsafe("", name, type, value);
    }

    public static String renderMagmaDefinitionUnsafe(String modifierString, String name, String type, String value) {
        return renderMagmaDefinitionUnsafe(modifierString, LET_KEYWORD_WITH_SPACE, name, type, value);
    }

    public static String renderMagmaDefinitionUnsafe(String modifierString, String mutabilityString, String name, String type, String value) {
        return modifierString + mutabilityString + name + " : " + type + " " + VALUE_SEPARATOR + " " + value + ";";
    }

    private static String renderBlock(String content) {
        return BLOCK_START + content + BLOCK_END;
    }

    public static String renderMagmaImportUnsafe(String parent, String child) {
        return IMPORT_KEYWORD_WITH_SPACE + "{ " + child + " } from " + parent + STATEMENT_END;
    }

    public static String renderMagmaFunctionUnsafe(String name) {
        return renderMagmaFunctionUnsafe("", name);
    }

    public static String renderMagmaFunctionUnsafe(String modifiersString, String name) {
        return renderMagmaFunctionUnsafe(modifiersString, name, "");
    }

    public static String renderMagmaFunctionUnsafe(String modifiersString, String name, String content) {
        return modifiersString + CLASS_KEYWORD_WITH_SPACE + "def " + name + "() =>" + " " + renderBlock(content);
    }

    public static JavaString run(JavaString input) {
        var lines = split(input);

        var imports = lines.subList(0, lines.size() - 1);
        var classString = lines.get(lines.size() - 1);

        var beforeString = imports.stream()
                .map(Compiler::compileImport)
                .map(option -> option.orElse(JavaString.EMPTY))
                .reduce(JavaString::concat)
                .orElse(JavaString.EMPTY);

        var compiledClass = compileClass(classString).$();
        return beforeString.concat(compiledClass);
    }

    private static Result<JavaString, UnsupportedOperationException> compileClass(JavaString classString) {
        return classString.firstRangeOfSlice(CLASS_KEYWORD_WITH_SPACE).flatMap(classIndex -> {
            return classString.firstIndexOfChar(BLOCK_START).flatMap(contentStart -> {
                return classString.lastIndexOfChar(BLOCK_END).flatMap(contentEnd -> {
                    var nameStart = classIndex.endIndex();

                    var className = classString.sliceBetween(nameStart, contentStart).strip();
                    var modifierString = classString.value().startsWith(PUBLIC_KEYWORD_WITH_SPACE)
                            ? new JavaString(EXPORT_KEYWORD_WITH_SPACE)
                            : JavaString.EMPTY;

                    return contentStart.next().map(afterContentStart -> {
                        var inputContent = classString.sliceBetween(afterContentStart, contentEnd);
                        var outputContent = compileDefinition(inputContent);
                        JavaString result;
                        if (outputContent.isEmpty()) {
                            result = renderMagmaFunction(modifierString, className, JavaString.EMPTY);
                        } else {
                            var content = outputContent.get();
                            var instanceValue = content.findInstanceValue().orElse(JavaString.EMPTY);
                            var instanceFunction = renderMagmaFunction(modifierString, className, instanceValue);
                            var staticValueOptional = content.findStaticValue();

                            var objectString = staticValueOptional
                                    .map(staticValue -> renderObject(className, staticValue))
                                    .orElse(JavaString.EMPTY);

                            result = instanceFunction.concat(objectString);
                        }

                        return result;
                    });
                });
            });
        }).into(ThrowableOption::new).orElseThrow(() -> new UnsupportedOperationException("No class present."));
    }

    private static JavaString renderObject(JavaString className, JavaString staticValue) {
        return new JavaString(renderObjectUnsafe(className.value(), staticValue.value()));
    }

    private static JavaString renderMagmaFunction(JavaString modifierString, JavaString name, JavaString content) {
        return new JavaString(renderMagmaFunctionUnsafe(modifierString, name, content));
    }

    private static String renderMagmaFunctionUnsafe(JavaString modifierString, JavaString className, JavaString content) {
        return renderMagmaFunctionUnsafe(modifierString.value(), className.value(), content.value());
    }

    private static Optional<StateResult> compileDefinition(JavaString inputContent) {
        return compileDefinition(inputContent.value());
    }

    private static List<JavaString> split(JavaString input) {
        return split(input.value()).stream().map(JavaString::new).collect(Collectors.toList());
    }

    private static ArrayList<String> split(String input) {
        var lines = new ArrayList<String>();
        var builder = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (c == ';' && depth == 0) {
                lines.add(builder.toString());
                builder = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
                builder.append(c);
            }
        }

        lines.add(builder.toString());
        return lines;
    }

    private static Option<JavaString> compileImport(JavaString beforeString) {
        return beforeString.firstRangeOfSlice(IMPORT_KEYWORD_WITH_SPACE).flatMap(importString -> {
            var segmentStart = beforeString.firstRangeOfSlice(IMPORT_KEYWORD_WITH_SPACE + STATIC_KEYWORD_WITH_SPACE)
                    .map(Range::endIndex)
                    .orElse(importString.endIndex());

            var set = beforeString.sliceFrom(segmentStart);
            return set.lastIndexOfChar('.').flatMap(last -> {
                var parent = set.sliceTo(last);

                return last.next().map(nextLast -> {
                    var child = set.sliceFrom(nextLast);

                    return renderMagmaImport(parent, child);
                });
            });
        });
    }

    private static JavaString renderMagmaImport(JavaString parent, JavaString child) {
        return new JavaString(renderMagmaImportUnsafe(parent.value(), child.value()));
    }

    private static Optional<StateResult> compileDefinition(String inputContent) {
        var valueSeparatorIndex = inputContent.indexOf(VALUE_SEPARATOR);
        if (valueSeparatorIndex == -1) return Optional.empty();

        var before = inputContent.substring(0, valueSeparatorIndex).strip();
        var separator = before.lastIndexOf(' ');

        var name = before.substring(separator + 1);

        var modifiersAndType = before.substring(0, separator);

        Set<String> modifiers;
        String inputType;

        var lastIndex = modifiersAndType.indexOf(' ');
        if (lastIndex == -1) {
            inputType = modifiersAndType;
            modifiers = Collections.emptySet();
        } else {
            var modifiersString = modifiersAndType.substring(0, lastIndex).strip();
            var typeString = modifiersAndType.substring(lastIndex + 1).strip();

            modifiers = new HashSet<>(Arrays.asList(modifiersString.split(" ")));
            inputType = typeString;
        }

        var outputType = compileType(inputType);

        var after = inputContent.substring(valueSeparatorIndex + 1, inputContent.lastIndexOf(STATEMENT_END)).strip();
        var modifierString = modifiers.isEmpty() ? "" : modifiers.stream()
                .filter(modifier -> modifier.equals(PUBLIC_KEYWORD))
                .map(modifier -> modifier + " ")
                .collect(Collectors.joining());

        var mutabilityString = modifiers.contains(FINAL_KEYWORD)
                ? CONST_KEYWORD_WITH_SPACE
                : LET_KEYWORD_WITH_SPACE;

        var rendered = renderMagmaDefinition(modifierString, mutabilityString, name, outputType, after);
        return Optional.of(modifiers.contains(STATIC_KEYWORD) ? new StaticResult(rendered) : new InstanceResult(rendered));
    }

    private static JavaString renderMagmaDefinition(String modifierString, String mutabilityString, String name, String outputType, String after) {
        return new JavaString(renderMagmaDefinitionUnsafe(modifierString, mutabilityString, name, outputType, after));
    }

    private static String compileType(String inputType) {
        return switch (inputType) {
            case INT_KEYWORD -> I32_KEYWORD;
            case LONG_KEYWORD -> I64_KEYWORD;
            default -> inputType;
        };
    }

    public static String renderJavaClass(String name) {
        return renderJavaClass("", name);
    }

    public static String renderJavaClass(String modifiersString, String name) {
        return renderJavaClass(modifiersString, name, "");
    }

    public static String renderJavaClass(String modifiersString, String name, String content) {
        return modifiersString + CLASS_KEYWORD_WITH_SPACE + name + " " + renderBlock(content);
    }

    public static String renderJavaImport(String parent, String child) {
        return renderJavaImport(parent, child, "");
    }

    public static String renderJavaImport(String parent, String child, String modifierString) {
        return IMPORT_KEYWORD_WITH_SPACE + modifierString + parent + "." + child + STATEMENT_END;
    }

    public static String renderObjectUnsafe(String name, String content) {
        return "object " + name + " " + BLOCK_START + content + BLOCK_END;
    }
}
