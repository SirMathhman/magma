package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Head;

public final class NativeStringHead implements Head<Character> {
    private final String value;
    private int count = 0;

    public NativeStringHead(String value) {
        this.value = value;
    }

    @Override
    public Option<Character> next() {
        if(count < value.length()) {
            final var c = value.charAt(count);
            count++;
            return new Some<>(c);
        } else {
            return new None<>();
        }
    }
}
