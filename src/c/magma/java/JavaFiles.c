#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct JavaFiles {
	void temp(){try (final var stream = Files.walk(directory)) {
            return new Ok<>(stream.toList());
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    public static Optional<IOException> createDirectories(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    public static Optional<IOException> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    public static Result<String, IOException> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
	}
};