package com.meti;

public final class CRenderer {
    private final String name;
    private final String type;
    private final int value;

    public CRenderer(String name, String type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String render() {
        return renderFunctionHeader(type) + renderBlock();
    }

    private String renderFunctionHeader(String input) {
        String typeString;
        if (input.equals("I16")) {
            typeString = "int";
        } else {
            typeString = "unsigned int";
        }
        return typeString + " " + name + "()";
    }

    private String renderBlock() {
        return "{" + renderReturn() + "}";
    }

    private String renderReturn() {
        return "return " + renderIntegerValue() + ";";
    }

    private String renderIntegerValue() {
        return String.valueOf(value);
    }
}
