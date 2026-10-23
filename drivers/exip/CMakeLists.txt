# exip-driver: the exip adapter for rexi-interop, linked against the exip
# libraries built by scripts/fetch-third-party.sh --build (Release).
#
#   cmake -S drivers/exip -B build/exip -DEXIP_DIR=<cache>/exip -DCMAKE_BUILD_TYPE=Release
#   cmake --build build/exip
cmake_minimum_required(VERSION 3.16)
project(exip_driver C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Release)
endif()

set(EXIP_DIR "" CACHE PATH "exip checkout (with build/ from fetch-third-party.sh --build)")
# The exip build tree; defaults to the cache layout. Point it elsewhere to test
# another exip build (e.g. a local working copy) against the same driver.
set(EXIP_BUILD_DIR "${EXIP_DIR}/build" CACHE PATH "exip CMake build directory")
if(NOT EXIP_DIR OR NOT EXISTS "${EXIP_BUILD_DIR}/src/common/libexip.common.a")
  message(FATAL_ERROR "set EXIP_DIR to an exip checkout built by scripts/fetch-third-party.sh --build "
                      "(or EXIP_BUILD_DIR to its build tree)")
endif()

add_executable(exip-driver exip_driver.c)
target_include_directories(exip-driver PRIVATE
  ${EXIP_DIR}/include
  ${EXIP_DIR}/src/common/include
  ${EXIP_DIR}/src/stringTables/include
  ${EXIP_DIR}/src/grammar/include
  ${EXIP_DIR}/src/streamIO/include
  ${EXIP_DIR}/src/contentIO/include
  ${EXIP_DIR}/platform/gcc/pc)
# Newer exip generates the build configuration into the build tree;
# older exip pins use platform/gcc/pc/exipConfig.h instead.
if(EXISTS "${EXIP_BUILD_DIR}/generated/include/exip_build_config.h")
  target_include_directories(exip-driver PRIVATE ${EXIP_BUILD_DIR}/generated/include)
  target_compile_definitions(exip-driver PRIVATE EXIP_HAVE_BUILD_CONFIG=1)
endif()
target_compile_options(exip-driver PRIVATE -Wall -Wextra)

set(EXIP_BUILD ${EXIP_BUILD_DIR}/src)
find_package(Threads REQUIRED)
target_link_libraries(exip-driver PRIVATE
  -Wl,--start-group
  ${EXIP_BUILD}/contentIO/libexip.contentIO.a
  ${EXIP_BUILD}/grammarGen/libexip.grammarGen.a
  ${EXIP_BUILD}/grammar/libexip.grammar.a
  ${EXIP_BUILD}/streamIO/libexip.streamIO.a
  ${EXIP_BUILD}/common/libexip.common.a
  -Wl,--end-group
  Threads::Threads m)
# Newer exip (with compression) inflates with zlib.
find_package(ZLIB QUIET)
if(ZLIB_FOUND)
  target_link_libraries(exip-driver PRIVATE ZLIB::ZLIB)
endif()
