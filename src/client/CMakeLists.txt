add_executable(donceykongjr main.c)
target_link_libraries(donceykongjr PRIVATE SDL2::SDL2 X11::X11 PkgConfig::JSON-C)
