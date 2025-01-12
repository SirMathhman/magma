package magma.stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadedStreamTest {
    @Test
    void map() {
        final var i = new HeadedStream<>(new LengthHead(3))
                .map(value -> value + 1)
                .foldLeft(0, Integer::sum);
        assertEquals(6, i);
    }

    @Test
    void flatMap() {
    }

    @Test
    void foldLeft() {
        final var i = new HeadedStream<>(new LengthHead(3))
                .foldLeft(0, Integer::sum);
        assertEquals(3, i);
    }

    @Test
    void concat() {
        final var i = new HeadedStream<>(new LengthHead(3))
                .concat(new HeadedStream<>(new LengthHead(10)))
                .foldLeft(0, Integer::sum);
        assertEquals(48, i);
    }

    @Test
    void filter() {
        final var sum = new HeadedStream<>(new LengthHead(6))
                .filter(value -> value % 2 == 0)
                .foldLeft(0, Integer::sum);
        assertEquals(6, sum);
    }
}