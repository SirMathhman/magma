package magma.app.compile;

public record CompileResult(Node beforePass, Node afterPass, String output) {
}
