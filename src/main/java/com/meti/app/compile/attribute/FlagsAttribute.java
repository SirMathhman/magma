package com.meti.app.compile.attribute;

import com.meti.api.collect.JavaList;
import com.meti.app.compile.common.Field;

public record FlagsAttribute(JavaList<Field.Flag> flags) implements Attribute {
    @Override
    public com.meti.api.collect.Stream<Field.Flag> asStreamOfFlags1() throws AttributeException {
        return flags.stream();
    }
}