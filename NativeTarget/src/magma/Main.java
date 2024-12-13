package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;class def Main() => {
public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "Source", "src", "magma", "Main.java");
            final var input = Files.readString(source);

            final var targetDirectory = Paths.get(".", "CompiledTarget", "src", "magma");
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);}

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
