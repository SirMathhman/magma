package magma.api.stream;package java.util.Optional;public final class RangeHead implements Head<Integer> {private final int extent;private int counter = 0;public RangeHead(int extent){this.extent = extent;}@Override
    public Optional<Integer> next();}