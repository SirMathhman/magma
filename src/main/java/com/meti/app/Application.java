package com.meti.app;

import com.meti.api.collect.JavaMap;
import com.meti.api.io.Path;
import com.meti.app.compile.CMagmaCompiler;
import com.meti.app.compile.Output;
import com.meti.app.compile.clang.CFormat;
import com.meti.app.module.Module;
import com.meti.app.source.Packaging;
import com.meti.app.source.Source;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.meti.api.io.NIOPath.Root;

public class Application {
    public static final Path Out = Root.resolveChild("out");
    protected static final String AppName = "Application";
    protected final Module module;

    public Application(Module module) {
        this.module = module;
    }

    void run() throws ApplicationException {
        var sources = listSources();
        var inputMap = new HashMap<Packaging, String>();
        for (var source : sources) {
            try {
                inputMap.put(source.computePackage(), source.read());
            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }
        var outputMap = new CMagmaCompiler(new JavaMap<>(inputMap)).compile();
        var targets = new HashSet<String>();
        for (Packaging aPackaging : outputMap.keySet()) {
            var output = compile(aPackaging, outputMap.get(aPackaging));
            targets.addAll(output);
        }

        build(targets);
    }

    private List<Source> listSources() throws ApplicationException {
        List<Source> sources;
        try {
            sources = module.listSources();
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
        return sources;
    }

    private static Set<String> compile(Packaging packaging_, Output<String> output) throws ApplicationException {
        try {
            Out.ensureAsDirectory();

            var packagePath = packaging_.streamParent()
                    .reduce(Out, Path::resolveChild, (previous, next) -> next);
            packagePath.ensureAsDirectory();

            var targets = new HashSet<String>();
            var formats = output.streamFormats().collect(Collectors.toList());
            for (CFormat format : formats) {
                var target = packagePath
                        .resolveChild(packaging_.computeName() + "." + format.getExtension())
                        .ensureAsFile()
                        .writeAsString(output.apply(format, ""));
                targets.add(Out.relativize(target)
                        .asString()
                        .replace("\\", "//"));
            }
            return targets;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    protected void build(Collection<String> targets) throws ApplicationException {
    }
}