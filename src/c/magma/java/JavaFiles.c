#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct JavaFiles {
	void temp(){
	to = from;catch (IOException e) {
            return new Err<>(e);
        }
	}
	void temp(){try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        }catch (IOException e) {
            return Optional.of(e);
        }
	}
	void temp(){try {
            Files.writeString(target, output);
            return Optional.empty();
        }catch (IOException e) {
            return Optional.of(e);
        }
	}
	void temp(){try {
            return new Ok<>(Files.readString(source));
        }catch (IOException e) {
            return new Err<>(e);
        }
	}
};