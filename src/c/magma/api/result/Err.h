import magma.api.Tuple;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Err<T, X>(X error) implements Result<T, X> {@Override
    public <R> Result<RX> flatMapValue(Function<T Result<RX>> mapper){return new Err<>(this.error);}@Override
    public <R> Result<RX> mapValue(Function<TR> mapper){return new Err<>(this.error);}@Override
    public <R> Result<TR> mapErr(Function<XR> mapper){return new Err<>(mapper.apply(this.error));}@Override
    public <R> R match(Function<TR> onOk Function<XR> onErr){return onErr.apply(this.error);}@Override
    public <R> Result<Tuple<TR>X> and(Supplier<Result<RX>> other){return new Err<>(this.error);}@Override
    public <R> Result<T Tuple<XR>> or(Supplier<Result<TR>> other){return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));}@Override
    public Optional<T> findValue(){return Optional.empty();}@Override
    public boolean isOk(){return false;}@Override
    public Optional<X> findError(){return Optional.of(this.error);}}