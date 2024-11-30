package magma.app.compile;

import magma.Main;
import magma.app.assemble.Operator;
import magma.java.JavaList;

import java.util.List;

public class CASMLang {
    public static final String DATA_TYPE = "data";
    public static final String DATA_LABEL = "label";
    public static final String DATA_VALUE = "value";
    public static final String LABEL_TYPE = "label";
    public static final String LABEL_NAME = "name";
    public static final String LABEL_CHILDREN = "children";
    public static final String INSTRUCTION_TYPE = "instruction";
    public static final String INSTRUCTION_OPERATOR = "operator";
    public static final String INSTRUCTION_LABEL = "label";

    public static Node label(String name, List<Node> children) {
        return new MapNode(LABEL_TYPE).withString(LABEL_NAME, name).withNodeList(LABEL_CHILDREN, new JavaList<>(children));
    }

    public static Node instruct(Operator operator, String label) {
        return instruct(operator).withString(INSTRUCTION_LABEL, label);
    }

    public static Node instruct(Operator operator) {
        return new MapNode(INSTRUCTION_TYPE).withInt(INSTRUCTION_OPERATOR, operator.computeOpCode());
    }

    public static Node data(String label, int value) {
        return new MapNode(DATA_TYPE)
                .withString(DATA_LABEL, label)
                .withInt(DATA_VALUE, value);
    }

    public static Node instruct(Operator operator, int value) {
        return instruct(operator).withInt(Main.INSTRUCTION_ADDRESS_OR_VALUE, value);
    }
}
