import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
public struct Stream<T> {Optional<T> foldLeft(BiFunction<TTT> folder);<R> R foldLeft(R initial BiFunction<RTR> folder);<R> Stream<R> map(Function<TR> mapper);<R, X> Result<RX> foldLeftToResult(R initial BiFunction<RT Result<RX>> folder);}