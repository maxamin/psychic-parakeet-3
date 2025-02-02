/* see copyright notice in VWSLang.h */

#include "vwsl_ast.h"
#include "vwsl_codegen.h"
#include "vwsl_vm.h"
#include <llvm/IR/IRBuilder.h>
#include <llvm/IR/Constants.h>
#include <llvm/IR/GlobalVariable.h>
#include <llvm/IR/Instructions.h>
#include <llvm/ExecutionEngine/GenericValue.h>
#include <iostream>

using namespace llvm;
