add_executable(donceykongjr main.c)
target_link_libraries(donceykongjr PRIVATE m SDL2::SDL2 X11::X11 PkgConfig::json-c PkgConfig::SDL2_image)
target_compile_options(donceykongjr PRIVATE -Wall -Wextra -Werror)
