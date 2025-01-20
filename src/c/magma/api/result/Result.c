import magma.api.Tuple;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Result<T, X> {<R> Result<RX> flatMapValue(Function<T Result<RX>> mapper);<R> Result<RX> mapValue(Function<TR> mapper);<R> Result<TR> mapErr(Function<XR> mapper);<R> R match(Function<TR> onOk Function<XR> onErr);<R> Result<Tuple<TR>X> and(Supplier<Result<RX>> other);<R> Result<T Tuple<XR>> or(Supplier<Result<TR>> other);Optional<T> findValue();boolean isOk();Optional<X> findError();}