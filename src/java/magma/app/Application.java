package magma.app;

import magma.app.io.unit.Unit;
import magma.app.io.source.SourceSet;
import magma.app.io.target.TargetSet;

import java.io.IOException;

public final class Application {
    private final TargetSet targetSet;
    private final SourceSet sourceSet;

    public Application(SourceSet sourceSet, TargetSet targetSet) {
        this.sourceSet = sourceSet;
        this.targetSet = targetSet;
    }

    private void runWithSource(Unit unit) throws IOException {
        targetSet.write(unit);
    }

    public void run() throws IOException {
        final var sources = sourceSet.collect();
        for (var source : sources) {
            runWithSource(source);
        }
    }
}