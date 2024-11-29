package magma;

import magma.api.option.Option;
import magma.app.compile.Layout;
import magma.java.JavaList;

public record MultipleLayout(JavaList<Layout> layouts) implements Layout {
    @Override
    public int computeElementSizeTo() {
        return layouts.stream()
                .map(Layout::computeElementSizeTo)
                .foldLeft(0, Integer::sum);
    }

    @Override
    public String toString() {
        final var joined = layouts.stream().map(Object::toString)
                .foldLeft((first, second) -> first + ", " + second)
                .orElse("");

        return "[" + joined + "]";
    }

    @Override
    public Option<Integer> computeElementSizeTo(JavaList<Integer> indices) {
        return indices.popFirst().map(first -> {
            final var index = first.left();
            final var after = first.right();

            final var beforeSize = layouts.sliceTo(index)
                    .map(stream -> stream.map(Layout::computeElementSizeTo).foldLeft(0, Integer::sum))
                    .orElse(0);

            final var thisSize = layouts.get(index)
                    .flatMap(inner -> inner.computeElementSizeTo(after))
                    .orElse(0);

            return beforeSize + thisSize;
        });
    }
}
