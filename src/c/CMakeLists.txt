cmake_minimum_required(VERSION 3.10)

project(Magma C)

set(CMAKE_C_COMPILER clang)

add_executable(Magma 
	./magma/CompileException.h
	./magma/CompileException.c
	./magma/FirstLocator.h
	./magma/FirstLocator.c
	./magma/LastLocator.h
	./magma/LastLocator.c
	./magma/Main.h
	./magma/Main.c
	./magma/Locator.h
	./magma/Locator.c
	./magma/Tuple.h
	./magma/Tuple.c
)
