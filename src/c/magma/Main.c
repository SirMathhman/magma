import magma.api.Tuple;
import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Options;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.Node;
import magma.app.compile.SymbolRule;
import magma.app.compile.TypeSplitter;
import magma.app.compile.rule.*;
import magma.app.error.ApplicationError;
import magma.app.error.Error;
import magma.app.error.FormattedError;
import magma.app.error.JavaError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
struct ", new InfixRule {}