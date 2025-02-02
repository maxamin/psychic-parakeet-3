/* see copyright notice in VWSLang.h */

#ifndef VWSL_CODEGEN_H
#define VWSL_CODEGEN_H

#include "vwsl_ast.h"
#include <llvm/ExecutionEngine/ExecutionEngine.h>
#include <llvm/ExecutionEngine/GenericValue.h>
#include <llvm/IR/Function.h>
#include <llvm/IR/IRBuilder.h>
#include <list>
#include <vector>

using namespace llvm;
class vwslVM;

struct vwslCodeGenBlock
{
    BasicBlock* m_bblock;
    BasicBlock* m_leave;
    NodeAST* m_ast;
    enum BlockType
    {
        CODE,
        IF_THEN,
        IF_ELSE,
        SWITCH,
        WHILE,
        DOWHILE,
        FOR,
        FOREACH,
        FUNCTION
    };
    BlockType m_type;
    Value* m_retVar;
    Value* m_thisVar;
    Value* m_argArray;
    Value* m_tmpArray;
    int m_tmpArraySize;
    std::list<std::pair<std::string, Value*> > m_localVars;
    bool m_isBlockEnd;
    std::list<std::string>* m_stringList;
};

class vwslCodeGenerator
{
    friend class vwslVM;
protected:
    vwslVM* m_vm;
    BlockAST* m_programBlockAST;
    llvm::Function* m_mainFunction;
    IRBuilder<> m_irBuilder;

    std::list<std::string> m_stringList;

    std::list<vwslCodeGenBlock*> m_blocks;

    //for error handling at compile-time
    bool m_isCompileError;

public:
    vwslCodeGenerator(vwslVM* vm);
    ~vwslCodeGenerator();

    vwslVM* getVM() { return m_vm; }
    llvm::LLVMContext& getLLVMContext();
    llvm::Module* getModule();
    IRBuilder<>& getIRBuilder() { return m_irBuilder; }

    void setProgramBlockAST(BlockAST* block) { m_programBlockAST = block; }
    BlockAST* getProgramBlockAST() { return m_programBlockAST; }

    void generateCode();
    llvm::GenericValue runCode();

    vwslCodeGenBlock* currentBlock() { return m_blocks.front(); }
    void pushBlock(BasicBlock* bblock, BasicBlock* leave,
                   NodeAST* ast, vwslCodeGenBlock::BlockType type,
                   Value* retVar, Value* thisVar, Value* argArray,
                   Value* tmpArray, int tmpArraySize,
                   std::list<std::string>* strList);
    void popBlock();

    Value* createStringPtr(const std::string& str, vwslCodeGenBlock* block, IRBuilder<>& builder);

    void setCompileError(bool v) { m_isCompileError = v; }
    bool isCompileError() { return m_isCompileError; }
protected:
    void eraseMainFunction();
};

#endif //VWSL_CODEGEN_H
