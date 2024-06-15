package magma.compile.rule;

import magma.compile.attribute.Attributes;
import magma.compile.attribute.StringAttribute;

import java.util.function.Function;

public record Node(String type, Attributes attributes) {
    public Node withString(String key, String value) {
        return mapAttributes(attributes -> attributes.with(key, new StringAttribute(value)));
    }

    public String formatWithDepth(int depth) {
        return "\t".repeat(depth) + format(depth);
    }

    public String format(int depth) {
        return type + " = " + attributes.format(depth);
    }

    @Override
    public String toString() {
        return formatWithDepth(0);
    }

    public boolean is(String type) {
        return this.type.equals(type);
    }

    public Node mapAttributes(Function<Attributes, Attributes> mapper) {
        return new Node(type, mapper.apply(attributes));
    }

    public Node retype(String type) {
        return new Node(type, attributes);
    }

    public Node withAttributes(Attributes attributes) {
        return new Node(type, attributes);
    }
}
