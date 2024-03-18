package com.meti.compile.rule;

import com.meti.collect.option.Option;
import com.meti.collect.option.Some;
import com.meti.collect.stream.Stream;
import com.meti.collect.stream.Streams;
import com.meti.java.JavaString;

import static com.meti.collect.option.None.None;

public class LazyRule implements Rule {
    private Option<Rule> current = None();

    public void setCurrent(Rule current) {
        this.current = Some.Some(current);
    }

    @Override
    public Stream<RuleResult> apply(JavaString input) {
        return current.map(value -> value.apply(input)).orElse(Streams.empty());
    }
}
