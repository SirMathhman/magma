package magma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public record Labels(List<Tuple<String, Label>> labels) {
    public Labels() {
        this(new ArrayList<>());
    }

    Labels updateLabel(String labelName, Function<Label, Label> mapper) {
        final var optional = findWithIndex(labelName);
        final var copy = new ArrayList<>(labels());
        if (optional.isPresent()) {
            var tuple = optional.get();
            final var index = tuple.left();
            final var oldLabel = tuple.right();

            final var newLabel = mapper.apply(oldLabel);
            copy.set(index, new Tuple<>(labelName, newLabel));
        } else {
            copy.add(new Tuple<>(labelName, mapper.apply(new Label())));
        }

        return new Labels(copy);
    }

    Optional<Tuple<Integer, Label>> findWithIndex(String labelName) {
        return IntStream.range(0, labels().size())
                .mapToObj(index -> new Tuple<>(index, labels().get(index)))
                .filter(tuple -> tuple.right().left().equals(labelName))
                .map(tuple -> tuple.mapRight(Tuple::right))
                .findFirst();
    }

    Optional<Long> resolveFunctionAddress(String destinationLabel) {
        var total = 0L;
        for (Tuple<String, Label> label : labels()) {
            if (label.left().equals(destinationLabel)) {
                return Optional.of(total);
            } else {
                total += label.right().size();
            }
        }

        return Optional.empty();
    }

    List<Instruction> flatten() {
        return labels().stream()
                .map(Tuple::right)
                .map(Label::instructions)
                .flatMap(Collection::stream)
                .toList();
    }
}