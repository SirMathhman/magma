package magma.app.error;package magma.app.error.context.Context;package java.util.ArrayList;package java.util.Collections;package java.util.Comparator;package java.util.List;package java.util.stream.Collectors;package java.util.stream.IntStream;public final class CompileError implements Error {private final String message;private final Context context;private final List<CompileError> children;public CompileError(String messageString message Context contextString message Context context List<CompileError> children){this.message = message;this.context = context;this.children = new ArrayList<>(children);}public CompileError(String messageString message Context context){this(message, context, Collections.emptyList());}@Override
    public String display(){return format(0);}public int maxDepth(){return 1 + this.children.stream()
                .mapToInt(CompileError::maxDepth)
                .max()
                .orElse(0);}private String format(int depth);}