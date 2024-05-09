package com.meti.rule;

import com.meti.Tuple;
import com.meti.node.MapNode;
import com.meti.node.NodeAttributes;

import java.util.Optional;

public abstract class SplitBySliceRule implements Rule {
    protected final Rule leftRule;
    protected final String slice;
    protected final Rule rightRule;

    public SplitBySliceRule(Rule leftRule, String slice, Rule rightRule) {
        this.leftRule = leftRule;
        this.slice = slice;
        this.rightRule = rightRule;
    }

    protected abstract int computeRightOffset();

    protected abstract int applyOperation(String value);

    @Override
    public Optional<Tuple<NodeAttributes, Optional<String>>> fromString(String value) {
        var separator = applyOperation(value);
        if (separator == -1) return Optional.empty();

        var left = value.substring(0, separator + computeLeftOffset());
        var leftMap = leftRule.fromString(left).map(Tuple::left);
        if (leftMap.isEmpty()) return Optional.empty();

        var right = value.substring(separator + computeRightOffset());
        var rightMap = rightRule.fromString(right).map(Tuple::left);

        if (rightMap.isEmpty()) return Optional.empty();

        var attributes = leftMap.orElseThrow().add(rightMap.orElseThrow());
        return Optional.of(new Tuple<>(attributes, Optional.empty()));
    }

    protected abstract int computeLeftOffset();

    @Override
    public Optional<String> toString(MapNode node) {
        return leftRule.toString(node).flatMap(leftResult ->
                rightRule.toString(node).map(rightResult ->
                        leftResult + computeRight(rightResult)));
    }

    protected abstract String computeRight(String rightResult);
}
