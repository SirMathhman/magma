import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct FirstLocator(String infix) implements Locator {@Override
    public Option<Integer> locate(String input) {
        final var index = input.indexOf(infix());
        if (index == -1) return new None<>();
        return new Some<>(index);
    }@Override
    public int sliceLength() {
        return this.infix.length();
    }
}