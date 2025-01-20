package magma.app.error.context;public record StringContext(String value) implements Context {@Override
    public String display();}