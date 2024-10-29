package magma;

import magma.compile.Error_;
import magma.core.String_;

public class ApplicationError implements Error_ {
    private final Error_ error;

    public ApplicationError(Error_ error) {
        this.error = error;
    }

    @Override
    public String_ findMessage() {
        return error.findMessage();
    }
}
