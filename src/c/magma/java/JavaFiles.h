#include "../../magma/api/io/Path.h"
#include "../../java/nio/file/Files.h"
struct JavaFiles{
	boolean isRegularFile(Path path){
		return Files.isRegularFile(path.unwrap());
	}
}
