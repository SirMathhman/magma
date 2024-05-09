package com.meti.rule;

public final class SplitByFirstSliceRule extends SplitBySliceRule {
    public SplitByFirstSliceRule(Rule leftRule, String slice, Rule rightRule) {
        super(leftRule, slice, rightRule);
    }

    public static Rule First(Rule parent, String slice, Rule child) {
        return new SplitByFirstSliceRule(parent, slice, child);
    }

    @Override
    protected int applyOperation(String value) {
        return value.indexOf(slice);
    }

    @Override
    protected int computeRightOffset() {
        return slice.length();
    }

    @Override
    protected String computeRight(String rightResult) {
        return slice + rightResult;
    }

    @Override
    protected int computeLeftOffset() {
        return 0;
    }
}