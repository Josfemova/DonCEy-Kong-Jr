cmake_minimum_required(VERSION 3.14.0 FATAL_ERROR)
project("DonCEy Kong Jr.")

find_package(X11 REQUIRED)
find_package(SDL2 REQUIRED)
find_package(PkgConfig REQUIRED)
find_package(Java 1.8 REQUIRED)

include(UseJava)
set(CMAKE_JAVA_COMPILE_FLAGS "-Xlint:all")

pkg_check_modules(json-c REQUIRED IMPORTED_TARGET json-c)
pkg_check_modules(SDL2_image REQUIRED IMPORTED_TARGET SDL2_image)

add_subdirectory(src)
