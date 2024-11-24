package magma;

public record StringRule(String propertyKey) {
    Result<String, ApplicationError> generate(Node node) {
        return node.findString(propertyKey())
                .<Result<String, ApplicationError>>map(Ok::new)
                .orElseGet(() -> new Err<>(ApplicationError.fromMessage("No string '" + propertyKey() + "' present", node.display())));
    }
}