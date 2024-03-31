package com.meti;

public class JavaLang {
    static String renderJavaClass(String name, String content) {
        return Compiler.renderJavaClass("", name, content);
    }

    static String renderDefinition(String name, String type) {
        return renderDefinition(name, type, "0");
    }

    static String renderDefinition(String name, String type, String value) {
        return renderDefinition("", name, type, value);
    }

    static String renderDefinition(String flagString, String name, String type, String value) {
        return flagString + type + " " + name + " = " + value + ";";
    }

    static String renderRecord(String name) {
        return "record " + name + "(){}";
    }
}