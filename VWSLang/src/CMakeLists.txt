SET(CMAKE_C_COMPILER "c++")

execute_process(COMMAND llvm-config --cxxflags OUTPUT_VARIABLE LLVM_CXX_FLAGS)
string(REGEX REPLACE "\n" " " LLVM_CXX_FLAGS "${LLVM_CXX_FLAGS}")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${LLVM_CXX_FLAGS} -g -w")

find_package(BISON)
find_package(FLEX)

BISON_TARGET(MyParser VWSL.y ${CMAKE_CURRENT_BINARY_DIR}/y.tab.c)
FLEX_TARGET(MyScanner VWSL.l ${CMAKE_CURRENT_BINARY_DIR}/lex.yy.c)
ADD_FLEX_BISON_DEPENDENCY(MyScanner MyParser)

include_directories(
    ${CMAKE_SOURCE_DIR}/src
    ${CMAKE_CURRENT_BINARY_DIR}
    )

set(SRC_LIST
    vwsl_ast.cpp
    vwsl_codegen.cpp
    vwsl_vm.cpp
    ${BISON_MyParser_OUTPUTS}
    ${FLEX_MyScanner_OUTPUTS}
    )

add_library(VWSLLIB
    ${SRC_LIST}
    )
