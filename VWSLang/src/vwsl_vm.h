/* see copyright notice in VWSLang.h */

#ifndef VWSL_VM_H
#define VWSL_VM_H

#include "vwsl_codegen.h"
#include <llvm/IR/LLVMContext.h>
#include <llvm/IR/Module.h>
#include <llvm/ExecutionEngine/ExecutionEngine.h>

using namespace llvm;

class vwslVM
{
protected:
    bool m_initOkay;

    LLVMContext m_llvmContext;

    //current module
    Module* m_module;
    //current engine
    ExecutionEngine* m_executionEngine;

    //finalized modules
    std::map<Module*, ExecutionEngine*> m_moduleEngineMap;
    //for GC. if count == 0, the module can be safely deleted.
    std::map<Module*, int> m_moduleFunctionCountMap;

    SegmentAST* m_ProgramBlockAST;
    vwslCodeGenerator* m_codeGenerator;

    //for debug
    bool m_printAST;
    bool m_printIR;

public:
    vwslVM();
    ~vwslVM();

    bool getInitOkay() { return m_initOkay; }

    LLVMContext& getLLVMContext() { return m_llvmContext; }

    Module* getCurrentModule() { return m_module; }
    ExecutionEngine* getCurrentExecutionEngine() { return m_executionEngine; }
    ExecutionEngine* getExecutionEngine(Module* mod);

    void releaseCurrentModuleEngine();
    void storeCurrentModuleEngine();
    void createNewModuleEngine();

    vwslCodeGenerator* getCodeGenerator() { return m_codeGenerator; }

    //for debug
    void setPrintAST(bool b) { m_printAST = b; }
    bool getPrintAST() { return m_printAST; }
    void setPrintIR(bool b) { m_printIR = b; }
    bool getPrintIR() { return m_printIR; }

public:
    //compile & run Code
    bool runCode(const char* code);
};

#endif //VWSL_VM_H
