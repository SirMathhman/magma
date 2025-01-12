package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.Optional;

public class JavaOptionals {
    public static <T> Option<T> to(Optional<T> optional) {
        return optional.<Option<T>>map(Some::new).orElseGet(None::new);
    }

    public static <T> Optional<T> from(Option<T> option) {
        return option.map(Optional::of).orElseGet(Optional::empty);
    }
}
