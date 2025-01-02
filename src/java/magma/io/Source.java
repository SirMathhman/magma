package magma.io;

import magma.java.JavaFiles;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record Source(Path root, Path child) {
    public Result<String, IOException> read() {
        return JavaFiles.readString(child());
    }

    public boolean startsWithNamespace(List<String> requested) {
        final var computed = computeNamespace();
        if (requested.size() > computed.size()) return false;

        for (int i = 0; i < requested.size(); i++) {
            if (!requested.get(i).equals(computed.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<String> computeNamespace() {
        var list = new ArrayList<String>();
        final var relativized = root.relativize(child);
        final var parent = relativized.getParent();
        for (int i = 0; i < parent.getNameCount(); i++) {
            list.add(parent.getName(i).toString());
        }
        return list;
    }

    public Path resolve(Path root, String extension) {
        final var relativized = this.root.relativize(child);
        final var parent = relativized.getParent();
        final var name = relativized.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        return root.resolve(parent).resolve(nameWithoutExt + extension);
    }
}
