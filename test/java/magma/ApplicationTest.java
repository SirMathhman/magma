package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static magma.result.Results.unwrap;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApplicationTest {
    private static Result<List<Long>, ApplicationException> run(List<Mnemonic> instructions) {
        if (instructions.isEmpty()) return new Ok<>(Collections.emptyList());
        return new Err<>(new ApplicationException());
    }

    @Test
    void noInput() {
        assertThrows(ApplicationException.class, () -> unwrap(run(List.of(Mnemonic.InputAndLoad))));
    }

    @Test
    void nothing() throws ApplicationException {
        assertIterableEquals(Collections.emptyList(), unwrap(run(Collections.emptyList())));
    }
}
