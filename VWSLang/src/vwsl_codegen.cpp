/* see copyright notice in VWSLang.h */

#include "vwsl_ast.h"
#include "vwsl_codegen.h"
#include "vwsl_vm.h"
#include "llvm/IR/LegacyPassManager.h"
#include <llvm/IR/Verifier.h>
#include <llvm/IR/IRPrintingPasses.h>
#include <llvm/IR/DerivedTypes.h>
#include <llvm/IR/IRBuilder.h>
#include <llvm/IR/Type.h>
#include <llvm/Support/raw_ostream.h>
#include <iostream>
#include <sstream>

using namespace llvm;

vwslCodeGenerator::vwslCodeGenerator(vwslVM* vm)
    : m_vm(vm)
    , m_programBlockAST(0)
    , m_mainFunction(0)
    , m_irBuilder(vm->getLLVMContext())
    , m_blocks()
{
}

vwslCodeGenerator::~vwslCodeGenerator()
{
}

LLVMContext& vwslCodeGenerator::getLLVMContext()
{
    return m_vm->getLLVMContext();
}

Module* vwslCodeGenerator::getModule()
{
    return m_vm->getCurrentModule();
}

void vwslCodeGenerator::generateCode()
{
    eraseMainFunction();

    if(m_programBlockAST == 0)
    {
        errs() << "ProgramBlock is NULL, ignore..." << "\n";
        return;
    }

    m_isCompileError = false;

    LLVMContext& context = m_vm->getLLVMContext();
    Module* module = m_vm->getCurrentModule();

    /* Create the top level interpreter function to call as entry */
    std::vector<Type*> argTypes;
    FunctionType* mainFuncType = FunctionType::get(IntegerType::get(context, 32), makeArrayRef(argTypes), false);

    static int count = 0;
    Twine mainName = Twine("main") + Twine(count++);

    m_mainFunction = Function::Create(mainFuncType, GlobalValue::InternalLinkage, mainName, module);

    //blocks
    BasicBlock* label_entry = BasicBlock::Create(context, "entry", m_mainFunction);
    BasicBlock* label_begin = BasicBlock::Create(context, "begin", m_mainFunction);
    BasicBlock* label_end = BasicBlock::Create(context, "end", m_mainFunction);
    
    m_irBuilder.SetInsertPoint(label_entry);

    //types
    Type* voidTy = Type::getVoidTy(context);
    PointerType* voidPtrTy = PointerType::get(Type::getInt8Ty(context), 0);
    IntegerType* int32Ty = IntegerType::get(context, 32);

    //consts
    ConstantInt* const_int32_0 = ConstantInt::get(context, APInt(32, 0));
    ConstantInt* const_int32_1 = ConstantInt::get(context, APInt(32, 1));

    //entry:
    m_irBuilder.CreateRet(const_int32_0);

    if(m_vm->getPrintIR())
    {
        errs() << ">>>LLVM IR<<<" << "\n";
        legacy::PassManager pm;
        pm.add(createPrintModulePass(outs()));
        pm.run(*module);
    }
}

GenericValue vwslCodeGenerator::runCode()
{
    if(m_mainFunction != 0)
    {
        ExecutionEngine* ee = m_vm->getCurrentExecutionEngine();
        ee->finalizeObject();

        std::vector<GenericValue> noargs;
        GenericValue v = ee->runFunction(m_mainFunction, noargs);
        if(v.IntVal.getZExtValue() == 0)
        {
            //m_msgHandler->error("Code was run.\n");
        }
        else
        {
            errs() << "Encounter error!" << "\n";;
        }

        //release unused data
        m_stringList.clear();

        return v;
    }

    return GenericValue();
}

void vwslCodeGenerator::pushBlock(BasicBlock* bblock, BasicBlock* leave,
    NodeAST* ast, vwslCodeGenBlock::BlockType type,
    Value* retVar, Value* thisVar, Value* argArray,
    Value* tmpArray, int tmpArraySize,
    std::list<std::string>* strList)
{
    vwslCodeGenBlock* block = new vwslCodeGenBlock();
    block->m_bblock = bblock;
    block->m_leave = leave;
    block->m_ast = ast;
    block->m_type = type;
    block->m_retVar = retVar;
    block->m_thisVar = thisVar;
    block->m_argArray = argArray;
    block->m_tmpArray = tmpArray;
    block->m_tmpArraySize = tmpArraySize;
    block->m_isBlockEnd = false;
    block->m_stringList = strList;
    m_blocks.push_front(block);
}

void vwslCodeGenerator::popBlock()
{
    vwslCodeGenBlock *top = m_blocks.front();
    m_blocks.pop_front();
    delete top;
}

llvm::Value* vwslCodeGenerator::createStringPtr(const std::string& str, vwslCodeGenBlock* block, IRBuilder<>& builder)
{
    std::list<std::string>* strList = block->m_stringList;
    std::list<std::string>::iterator it = strList->begin(), end = strList->end();

    const char* strPtr = 0;
    for(; it != end; ++it)
    {
        if((*it) == str)
        {
            strPtr = it->c_str();
            break;
        }
    }
    
    if(strPtr == 0)
    {
        strList->push_back(str);
        strPtr = strList->back().c_str();
    }

    return builder.CreateIntToPtr(
            (sizeof(const char*) == sizeof(uint64_t) ?
                builder.getInt64((uintptr_t)strPtr) :
                builder.getInt32((uintptr_t)strPtr)
            ),
            builder.getInt8PtrTy()
        );
}

void vwslCodeGenerator::eraseMainFunction()
{
    if(m_mainFunction != 0)
    {
        m_mainFunction = 0;
    }
}
