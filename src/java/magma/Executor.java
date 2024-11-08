package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.ApplicationError;
import magma.app.ThrowableError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Executor {
    public static final Path ROOT = Paths.get(".", "src", "magma");
    public static final Path TARGET = ROOT.resolveSibling("main.asm");
    public static final int INP = 0x00;
    public static final int LOAD = 0x01;
    public static final int STO = 0x02;
    public static final int OUT = 0x03;
    public static final int ADD = 0x04;
    public static final int SUB = 0x05;
    public static final int INC = 0x06;
    public static final int DEC = 0x07;
    public static final int TAC = 0x08;
    public static final int JMP = 0x09;
    public static final int HRS = 0x0A;
    public static final int SFT = 0x0B;
    public static final int SHL = 0x0C;
    public static final int SHR = 0x0D;
    public static final int TS = 0x0E;
    public static final int CAS = 0x0F;

    public static void main(String[] args) {
        readAndExecute().ifPresent(error -> System.err.println(error.format(0, 0)));
    }

    static Option<ApplicationError> readAndExecute() {
        return readSafe(TARGET)
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .match(input -> {
                    execute(formatInput(input));
                    return new None<>();
                }, Some::new);
    }

    // Helper function to create a valid 64-bit instruction
    private static long createInstruction(int opcode, long addressOrValue) {
        if (opcode < 0x00 || opcode > 0xFF) {
            throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
        }

        // Shift the opcode to the top 8 bits and combine with the addressOrValue
        return ((long) opcode << 56) | addressOrValue;
    }

    private static void execute(Deque<Integer> input) {
        final List<Long> memory = new ArrayList<>();
        memory.add(createInstruction(INP, 1L));

        long accumulator = 0;  // Holds current value for operations
        int programCounter = 0;

        while (programCounter < memory.size()) {
            final long instruction = memory.get(programCounter);

            // Decode the instruction
            int opcode = (int) ((instruction >> 56) & 0xFF);  // First 8 bits
            long addressOrValue = instruction & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

            programCounter++;  // Move to next instruction by default

            // Execute based on opcode
            switch (opcode) {
                case INP:  // INP
                    if (!input.isEmpty()) {
                        accumulator = input.poll();
                    } else {
                        System.err.println("Input queue is empty.");
                    }
                    break;
                case LOAD:  // LOAD
                    accumulator = addressOrValue < memory.size() ? memory.get((int) addressOrValue) : 0;
                    break;
                case STO:  // STO
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, accumulator);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case OUT:  // OUT
                    System.out.println("Output: " + accumulator);
                    break;
                case ADD:  // ADD
                    if (addressOrValue < memory.size()) {
                        accumulator += memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case SUB:  // SUB
                    if (addressOrValue < memory.size()) {
                        accumulator -= memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case INC:  // INC
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, memory.get((int) addressOrValue) + 1);
                    }
                    break;
                case DEC:  // DEC
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, memory.get((int) addressOrValue) - 1);
                    }
                    break;
                case TAC:  // TAC
                    if (accumulator < 0) {
                        programCounter = (int) addressOrValue;
                    }
                    break;
                case JMP:  // JMP
                    programCounter = (int) addressOrValue;
                    break;
                case HRS:  // HRS
                    return;  // Halt execution
                case SFT:  // SFT
                    int leftShift = (int) ((addressOrValue >> 8) & 0xFF);
                    int rightShift = (int) (addressOrValue & 0xFF);
                    accumulator = (accumulator << leftShift) >> rightShift;
                    break;
                case SHL:  // SHL
                    accumulator <<= addressOrValue;
                    break;
                case SHR:  // SHR
                    accumulator >>= addressOrValue;
                    break;
                case TS:  // TS
                    if (addressOrValue < memory.size()) {
                        if (memory.get((int) addressOrValue) == 0) {
                            memory.set((int) addressOrValue, 1L);
                        } else {
                            programCounter--;  // Retry this instruction if lock isn't available
                        }
                    }
                    break;
                case CAS:  // CAS
                    long oldValue = memory.get((int) addressOrValue);
                    long compareValue = (addressOrValue >> 32) & 0xFFFFFFFFL;
                    long newValue = addressOrValue & 0xFFFFFFFFL;
                    if (oldValue == compareValue) {
                        memory.set((int) addressOrValue, newValue);
                    }
                    break;
                default:
                    System.err.println("Unknown opcode: " + opcode);
                    break;
            }
        }

        System.out.println("Final Memory State: " + memory);
        System.out.println("Final Accumulator: " + accumulator);
    }

    private static LinkedList<Integer> formatInput(String content) {
        return Arrays.stream(content.split("\\R"))
                .filter(value -> !value.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
