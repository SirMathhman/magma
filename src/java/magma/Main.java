package magma;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var input = new LinkedList<Integer>(List.of(

        ));

        var memory = new ArrayList<Integer>();
        var programCounter = 0;

        while (programCounter < memory.size()) {
            final var instruction = memory.get(programCounter);
            final var opCode = instruction << 24;
            final var addressOrValue = instruction & 0x00FFFFFF;

            programCounter++;

            final var operation = Operation.values()[opCode];
            switch (operation) {
            }
        }
    }
}
