package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;

public interface TargetSet {
    void write(Unit unit, String output) throws IOException;
}
