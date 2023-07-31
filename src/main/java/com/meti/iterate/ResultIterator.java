package com.meti.iterate;

import com.meti.core.Ok;
import com.meti.core.Option;
import com.meti.core.Result;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ResultIterator<T, E extends Throwable> extends AbstractIterator<Result<T, E>> {
    private final Iterator<Result<T, E>> parent;

    public ResultIterator(Iterator<Result<T, E>> parent) {
        this.parent = parent;
    }

    public <C> Result<C, E> collectAsResult(Collector<T, C> collector) {
        return parent.collect(new Collector<>() {
            @Override
            public Result<C, E> initial() {
                return Ok.apply(collector.initial());
            }

            @Override
            public Result<C, E> foldLeft(Result<C, E> accumulated, Result<T, E> element) {
                return accumulated.mapValueToResult(accumulatedValue ->
                        element.mapValue(elementValue ->
                                collector.foldLeft(accumulatedValue, elementValue)));
            }
        });
    }

    @Override
    public Option<Result<T, E>> head() {
        return parent.head();
    }

    public <C> Result<C, E> foldLeftInner(C initial, BiFunction<C, T, C> folder) {
        return parent.foldLeft(Ok.apply(initial), (ceResult, teResult) -> ceResult.mapValueToResult(accumulated -> teResult.mapValue(element -> folder.apply(accumulated, element))));
    }

    public <R> ResultIterator<R, E> mapInner(Function<T, R> mapper) {
        return new ResultIterator<>(parent.map(teResult -> teResult.mapValue(mapper)));
    }
}
