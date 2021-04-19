cmake_minimum_required(VERSION 3.14.0 FATAL_ERROR)
project("DonCEy Kong Jr.")

find_package(X11 REQUIRED)
find_package(SDL2 REQUIRED)
find_package(PkgConfig REQUIRED)
find_package(Java 1.8 REQUIRED)

include(UseJava)
pkg_check_modules(JSON-C REQUIRED IMPORTED_TARGET json-c)

add_subdirectory(src)
