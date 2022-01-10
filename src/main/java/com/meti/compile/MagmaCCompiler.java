package com.meti.compile;

import com.meti.compile.clang.CRenderer;
import com.meti.compile.common.block.Splitter;
import com.meti.compile.magma.MagmaLexer;
import com.meti.compile.node.Node;
import com.meti.compile.node.Text;

import java.util.ArrayList;
import java.util.stream.Collectors;

public record MagmaCCompiler(String input) {
    public String compile() throws CompileException {
        if (input.isBlank()) return input;
        var root = new Text(this.input);
        var input = new Splitter(root).split().collect(Collectors.toList());
        var lines = new ArrayList<Node>();
        for (Text oldLine : input) {
            lines.add(new MagmaLexer(oldLine).lex());
        }
        var builder = new StringBuilder();
        for (Node line : lines) {
            builder.append(new CRenderer(line).render().compute());
        }
        return builder.toString();
    }
}