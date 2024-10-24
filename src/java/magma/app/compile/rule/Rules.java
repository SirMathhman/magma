package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.CompileException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Rules {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);

    public static <T> Result<T, CompileException> await(Supplier<T> supplier) {
        try {
            return new Ok<>(CompletableFuture.supplyAsync(supplier).get(DEFAULT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new Err<>(new CompileException(e));
        }
    }
}
