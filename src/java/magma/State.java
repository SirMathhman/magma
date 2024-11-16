package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.value.Value;

import java.util.ArrayList;
import java.util.List;

class State {
    public final List<Value> memory;
    private final int counter;
    private final List<Value> input;

    State(List<Value> memory, List<Value> input) {
        this(memory, input, 0);
    }

    private State(List<Value> memory, List<Value> input, int counter) {
        this.counter = counter;
        this.memory = memory;
        this.input = input;
    }

    Result<State, ApplicationException> loadInputToMemory(int addressOrValue) {
        if (input.isEmpty()) return new Err<>(new ApplicationException("No more input present."));

        final var slice = input.subList(0, input.size() - 1);
        final var last = input.getLast();

        final var copy = new ArrayList<>(memory);
        copy.set(addressOrValue, last);
        return new Ok<>(new State(copy, slice, counter));
    }

    Option<Value> findCurrentInstruction() {
        if (counter < memory.size()) {
            return new Some<>(memory.get(counter));
        } else {
            return new None<>();
        }
    }

    public State advance() {
        return new State(memory, input, counter + 1);
    }
}
