package com.meti.stream;

import com.meti.C1E1;
import com.meti.EndOfStreamException;
import com.meti.F1E1;
import com.meti.F2E1;

public abstract class AbstractStream<T> implements Stream<T> {
    protected abstract T head() throws StreamException;

    @Override
    public <E extends Exception> void forEach(C1E1<T, E> consumer) throws E, StreamException {
        while (true) {
            try {
                consumer.apply(head());
            } catch (EndOfStreamException e) {
                break;
            }
        }
    }

    @Override
    public <R, E extends Exception> Stream<R> map(F1E1<T, R, E> mapper) {
        return new AbstractStream<>() {
            @Override
            protected R head() throws StreamException {
                try {
                    return mapper.apply(AbstractStream.this.head());
                } catch (EndOfStreamException e) {
                    throw e;
                } catch (Exception e) {
                    throw new StreamException(e);
                }
            }
        };
    }

    @Override
    public <R, E extends Exception> R foldRight(R initial, F2E1<R, T, R, E> folder) throws E, StreamException {
        var current = initial;
        while (true) {
            try {
                current = folder.apply(current, head());
            } catch (EndOfStreamException e) {
                break;
            }
        }
        return current;
    }

    @Override
    public <E extends Exception> Stream<T> filter(F1E1<T, Boolean, E> filter) {
        return new AbstractStream<>() {
            @Override
            protected T head() throws StreamException {
                try {
                    T head;
                    do {
                        head = AbstractStream.this.head();
                    } while (!filter.apply(head));
                    return head;
                } catch (EndOfStreamException e) {
                    throw e;
                } catch (Exception e) {
                    throw new StreamException(e);
                }
            }
        };
    }
}
