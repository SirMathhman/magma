package magma.app.locate;package java.util.Optional;public record FirstLocator(String infix) implements Locator {@Override
    public String unwrap(){return this.infix;}@Override
    public int length(){return this.infix.length();}@Override
    public Optional<Integer> locate(String input){final var index = input.indexOf(this.infix);return index == -1 ? Optional.empty() : Optional.of(index);}}