cmake_minimum_required(VERSION 3.10)

project(Magma C)

set(CMAKE_C_COMPILER clang)

add_executable(Magma ./magma/Main.h ./magma/Main.c)
