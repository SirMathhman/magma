import java.util.Optional;struct RangeHead implements Head<Integer>{
int extent;
int counter=0;
public RangeHead(int extent);
Optional<Integer> next();
}