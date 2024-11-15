package magma.app.compile.lang.casm;

import magma.app.compile.MapNode;
import magma.app.compile.Node;

import static magma.app.compile.lang.CASMLang.*;

public class Instructions {
    static Node instruct(String mnemonic, long value) {
        return instruct(mnemonic)
                .withInt(INSTRUCTION_ADDRESS_OR_VALUE, (int) value);
    }

    static Node instruct(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(INSTRUCTION_MNEMONIC, mnemonic);
    }

    static Node instruct(String mnemonic, String label) {
        return instruct(mnemonic).withString(INSTRUCTION_LABEL, label);
    }
}
