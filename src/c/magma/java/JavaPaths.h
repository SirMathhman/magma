#include "../../magma/api/io/Path.h"
#include "../../java/nio/file/Paths.h"
struct JavaPaths{
	Path get(String first, String... more){
		return new JavaPath(Paths.get(first, more));
	}
}
