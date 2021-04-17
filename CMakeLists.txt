cmake_minimum_required(VERSION 3.14.0 FATAL_ERROR)
project("DonCEy Kong Jr.")

find_package(Java 1.8 REQUIRED)
find_package(SDL2 REQUIRED)

include(UseJava)

add_subdirectory(src)
