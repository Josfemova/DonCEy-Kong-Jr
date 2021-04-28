add_executable(donceykongjr main.c game.c loop.c net.c init.c quit.c hash_map.c vec.c)
target_link_libraries(donceykongjr PRIVATE SDL2::SDL2 X11::X11 PkgConfig::json-c PkgConfig::SDL2_image PkgConfig::SDL2_ttf)
target_compile_options(donceykongjr PRIVATE -Wall -Wextra -Werror)
set_target_properties(donceykongjr PROPERTIES INTERPROCEDURAL_OPTIMIZATION TRUE)
