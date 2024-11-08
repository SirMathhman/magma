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
    public static final Path TARGET = ROOT.resolve("main.asm");
    public static final int INPUT_AND_LOAD = 0x00;
    public static final int INPUT_AND_STORE = 0x10;
    public static final int LOAD = 0x01;
    public static final int STO = 0x02;
    public static final int OUT = 0x03;
    public static final int ADD = 0x04;
    public static final int SUB = 0x05;
    public static final int INC = 0x06;
    public static final int DEC = 0x07;
    public static final int TAC = 0x08;
    public static final int JUMP = 0x09;
    public static final int HALT = 0x0A;
    public static final int SFT = 0x0B;
    public static final int SHL = 0x0C;
    public static final int SHR = 0x0D;
    public static final int TS = 0x0E;
    public static final int CAS = 0x0F;
    public static final int BYTES_PER_LONG = 8;

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

    private static void execute(Deque<Long> input) {
        System.out.println("Memory footprint: " + (input.size() * BYTES_PER_LONG) + " bytes");

        final List<Long> memory = new ArrayList<>();
        memory.add(createInstruction(INPUT_AND_STORE, 1L));

        long accumulator = 0;  // Holds current value for operations
        int programCounter = 0;

        while (programCounter < memory.size()) {
            print(memory);

            final long instructionUnsigned = memory.get(programCounter);

            // Decode the instruction
            int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);  // First 8 bits
            long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

            System.out.println(programCounter + " " + Long.toString(opcode, 16) + " " + Long.toString(addressOrValue, 16));

            programCounter++;  // Move to next instruction by default

            // Execute based on opcode
            switch (opcode) {
                case INPUT_AND_LOAD:  // INP
                    if (input.isEmpty()) {
                        throw new RuntimeException("Input queue is empty.");
                    } else {
                        accumulator = input.poll();
                    }
                    break;
                case INPUT_AND_STORE:  // INP
                    if (input.isEmpty()) {
                        throw new RuntimeException("Input queue is empty.");
                    } else {
                        final var polled = input.poll();
                        while (!(addressOrValue < memory.size())) {
                            memory.add(0L);
                        }

                        memory.set((int) addressOrValue, polled);
                        break;
                    }
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
                    System.out.println("Output: " + Long.toString(accumulator, 16));
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
                case JUMP:  // JMP
                    programCounter = (int) addressOrValue;
                    break;
                case HALT:  // HRS
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

    private static void print(List<Long> memory) {
        System.out.println(memory.stream()
                .map(value -> Long.toString(value, 16))
                .collect(Collectors.joining(", ", "[", "]")));
    }

    private static LinkedList<Long> formatInput(String content) {
        return Arrays.stream(content.split("\\R"))
                .filter(value -> !value.isEmpty())
                .map(Executor::createInstruction)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static long createInstruction(String line) {
        final var lineStripped = line.strip();
        final var separator = line.indexOf(' ');
        if (separator == -1) {
            return findOpCode(lineStripped)
                    .map(value -> createInstruction(value, 0))
                    .orElseGet(() -> parse(lineStripped));
        } else {
            final var opInstruction = line.substring(0, separator).strip();
            return findOpCode(opInstruction).map(opCode -> {
                final var addressOrValue = Long.parseLong(line.substring(separator + 1).strip());
                return createInstruction(opCode, addressOrValue);
            }).orElseGet(() -> parse(lineStripped));
        }
    }

    private static long parse(String stripped) {
        try {
            return Long.parseUnsignedLong(stripped, 16);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Unknown instruction: " + stripped);
        }
    }

    private static Option<Integer> findOpCode(String opInstruction) {
        return switch (opInstruction.toUpperCase()) {
            case "HALT" -> new Some<>(HALT);
            case "LOAD" -> new Some<>(LOAD);
            case "INPS" -> new Some<>(INPUT_AND_STORE);
            case "JMP" -> new Some<>(JUMP);
            case "OUT" -> new Some<>(OUT);
            default -> new None<>();
        };
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }
}
