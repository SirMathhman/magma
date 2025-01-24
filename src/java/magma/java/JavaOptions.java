package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.app.Node;

import java.util.Optional;

public class JavaOptions {
    public static Option<Node> fromNative(Optional<Node> optional) {
        if(optional.isPresent()) {
            return new Some<>(optional.get());
        } else {
            return new None<>();
        }
    }
}
