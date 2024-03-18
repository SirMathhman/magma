package com.meti.compile.rule;

import com.meti.collect.stream.Stream;
import com.meti.collect.stream.Streams;
import com.meti.java.JavaString;

public class ConjunctionRule implements Rule {
    private final Rule left;
    private final Rule right;

    public ConjunctionRule(Rule left, Rule right) {
        this.left = left;
        this.right = right;
    }

    public static Rule Join(Rule first, Rule... more) {
        return Streams.from(more).foldRight(first, (rule, rule2) -> new ConjunctionRule(rule2, rule2));
    }

    @Override
    public Stream<RuleResult> apply(JavaString input) {
        return input.streamIndices()
                .map(length -> {
                    var left = input.sliceTo(length);
                    var right = input.sliceFrom(length);

                    return this.left.apply(left)
                            .and(this.right.apply(right))
                            .map(tuple -> MapRuleResult.merge(tuple.a(), tuple.b()));
                })
                .flatMap(Streams::fromOption)
                .next();
    }
}
