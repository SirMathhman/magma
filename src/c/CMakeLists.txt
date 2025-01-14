cmake_minimum_required(VERSION 3.10)

project(MyProject C)

set(CMAKE_C_COMPILER clang)

add_executable(my_program ./magma/Main.h ./magma/Main.c)
