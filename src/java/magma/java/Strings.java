package magma.java;

import magma.stream.HeadedStream;
import magma.stream.LengthHead;
import magma.stream.Stream;

public class Strings {
    public static Stream<Character> streamChars(String value1) {
        return new HeadedStream<Integer>(new LengthHead(value1.length()))
                .map(value1::charAt);
    }
}