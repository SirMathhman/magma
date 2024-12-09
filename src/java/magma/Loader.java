package magma;

import java.util.List;

public interface Loader {
    List<Instruction> load(Stack stack);
}
