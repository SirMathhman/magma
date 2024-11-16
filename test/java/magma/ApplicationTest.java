package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.value.MnemonicValue;
import magma.value.NumericValue;
import magma.value.Value;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static magma.result.Results.unwrap;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApplicationTest {
    private static Result<List<Value>, ApplicationException> runWithNoInput(
            List<Value> initialMemory
    ) {
        return runWithNoInput(initialMemory, Collections.emptyList());
    }

    private static Result<List<Value>, ApplicationException> runWithNoInput(
            List<Value> initialMemory,
            List<Value> initialInput
    ) {
        if (initialMemory.isEmpty()) return new Ok<>(Collections.emptyList());

        final var memory = new ArrayList<>(initialMemory);
        List<Value> input = new ArrayList<>(initialInput);
        var counter = 0;

        while (counter < memory.size()) {
            final var value = memory.get(counter);
            counter++;

            final var mnemonic = value.findMnemonic();
            if (mnemonic.isEmpty()) return new Err<>(new ApplicationException("No mnemonic present: " + value));

            switch (mnemonic.orElse(Mnemonic.InputAndLoad)) {
                case InputAndLoad -> {
                    if (input.isEmpty()) return new Err<>(new ApplicationException("End of input"));

                    final var nextInput = input.getLast();
                    input = input.subList(0, input.size() - 1);
                    memory.set(0, nextInput);
                }
            }
        }

        return new Ok<>(memory);
    }

    @Test
    void inputAndLoad() throws ApplicationException {
        unwrap(runWithNoInput(List.of(new MnemonicValue(Mnemonic.InputAndLoad)), List.of(new NumericValue())));
    }

    @Test
    void inputAndLoadWithEmptyInput() {
        assertThrows(ApplicationException.class, () -> unwrap(runWithNoInput(List.of(new MnemonicValue(Mnemonic.InputAndLoad)))));
    }

    @Test
    void nothing() throws ApplicationException {
        assertIterableEquals(Collections.emptyList(), unwrap(runWithNoInput(Collections.emptyList())));
    }
}
