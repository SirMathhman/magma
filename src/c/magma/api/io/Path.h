#include "../../../magma/api/option/Option.h"
#include "../../../magma/api/result/Result.h"
#include "../../../magma/api/stream/Stream.h"
#include "../../../magma/java/JavaSet.h"
#include "../../../java/io/IOException.h"
struct Path{
	boolean isExists();
	Stream<Path> stream();
	Option<IOException> writeString(String output);
	Option<IOException> createAsDirectories();
	Result<String, IOException> readStrings();
	Result<JavaSet<Path>, IOException> walkWrapped();
	java.nio.file.Path unwrap();
	Path relativize(Path child);
	Option<Path> findParent();
	int getNameCount();
	Option<Path> getName(int index);
	String format();
	Path getFileName();
	Path resolvePath(Path child);
	Path resolveChild(String child);
}
