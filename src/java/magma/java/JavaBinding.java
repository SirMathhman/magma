package magma.java;

import magma.api.option.Option;

public record JavaBinding<A, B>(JavaMap<A, B> fromLeft, JavaMap<B, A> fromRight) {
    public JavaBinding() {
        this(new JavaMap<>(), new JavaMap<>());
    }

    public JavaBinding<A, B> set(A left, B right) {
        return new JavaBinding<>(fromLeft.put(left, right), fromRight.put(right, left));
    }

    public Option<B> findByLeft(A left) {
        return fromLeft.find(left);
    }

    public Option<A> findByRight(B right) {
        return fromRight.find(right);
    }
}
