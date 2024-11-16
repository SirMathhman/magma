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

import static magma.Mnemonic.LoadInputToMemory;
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
        return runLoop(new State(initialMemory, initialInput));
    }

    private static Result<List<Value>, ApplicationException> runLoop(State state) {
        var current = state;
        while (true) {
            final var option = current.findCurrentInstruction();
            if (option.isEmpty()) break;
            final var value = option.orElse(new NumericValue());

            var incremented = current.advance();
            final var mnemonic = value.findMnemonic();
            if (mnemonic.isEmpty()) return new Err<>(new ApplicationException("No mnemonic present: " + value));

            final var result = processMnemonic(mnemonic.orElse(LoadInputToMemory), incremented);
            if (result.isErr()) return result.replaceValue(new ArrayList<>());
            current = result.findValue().orElse(current);
        }
        return new Ok<>(current.memory);
    }

    private static Result<State, ApplicationException> processMnemonic(Mnemonic mnemonic, State state) {
        if (mnemonic == LoadInputToMemory) {
            return state.loadInputToMemory(0);
        }
        return new Err<>(new ApplicationException("Unknown mnemonic: " + mnemonic));
    }

    @Test
    void inputAndLoad() throws ApplicationException {
        unwrap(runWithNoInput(List.of(new MnemonicValue(LoadInputToMemory)), List.of(new NumericValue())));
    }

    @Test
    void inputAndLoadWithEmptyInput() {
        assertThrows(ApplicationException.class, () -> unwrap(runWithNoInput(List.of(new MnemonicValue(LoadInputToMemory)))));
    }

    @Test
    void nothing() throws ApplicationException {
        assertIterableEquals(Collections.emptyList(), unwrap(runWithNoInput(Collections.emptyList())));
    }

}
