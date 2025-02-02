/* see copyright notice in VWSLang.h */

#include "vwsl_vm.h"
#include "vwsl_token.h"
#include "y.tab.h"
#include <llvm/ExecutionEngine/MCJIT.h>
#include "llvm/ExecutionEngine/SectionMemoryManager.h"
#include <llvm/Support/Host.h>
#include <llvm/Support/TargetSelect.h>
#include <llvm/Support/ManagedStatic.h>
#include <stdio.h>
#include <string.h>
#include <memory>

using namespace llvm;

extern SegmentAST* ProgramHead;

vwslVM::vwslVM()
    : m_initOkay(true)
    , m_llvmContext()
    , m_module(0)
    , m_printAST(true)
    , m_printIR(false)
{
    InitializeNativeTarget();
    InitializeNativeTargetAsmPrinter();
    InitializeNativeTargetAsmParser();

    createNewModuleEngine();
    m_codeGenerator = new vwslCodeGenerator(this);
}

vwslVM::~vwslVM()
{
    delete m_codeGenerator;
    delete m_executionEngine;
    llvm::llvm_shutdown();
}

ExecutionEngine* vwslVM::getExecutionEngine(Module* mod)
{
    if(mod == m_module)
        return m_executionEngine;

    auto it = m_moduleEngineMap.find(mod);
    if(it == m_moduleEngineMap.end())
        return 0;

    return it->second;
}

void vwslVM::releaseCurrentModuleEngine()
{
    if(m_module != 0)
    {
        delete m_executionEngine;

        m_module = 0;
        m_executionEngine = 0;
    }
}

void vwslVM::storeCurrentModuleEngine()
{
    if(m_module != 0)
    {
        m_moduleEngineMap[m_module] = m_executionEngine;

        m_module = 0;
        m_executionEngine = 0;
    }
}

void vwslVM::createNewModuleEngine()
{
    storeCurrentModuleEngine();

    static int count = 0;
    char buf[16];
    sprintf(buf, "aclang jit %d", count++);
    std::unique_ptr<Module> mod(new Module(buf, m_llvmContext));
    m_module = mod.get();

    std::string errStr;
    m_executionEngine = 
    EngineBuilder(std::move(mod))
        .setErrorStr(&errStr)
        .create();
    if(m_executionEngine == NULL)
    {
        errs() << ": Failed to construct ExecutionEngine: " << errStr
               << "\n";
        m_initOkay = false;
    }
    else
    {
        // m_codeGenerator->createCoreFunctions();
    }
}

extern int column;
extern int yyparse();
extern void yyerror(const char* s);

bool vwslVM::runCode(const char* code)
{
    m_codeGenerator->setCompileError(false);

    if(m_module == NULL)
    {
        createNewModuleEngine();
    }

    bool compileOk = true;

    if(yyparse() != 0)
        compileOk = false;

    if(compileOk && !ProgramHead)
        compileOk = false;

    if(compileOk && m_printAST)
    {
        printf("========AST Begin========\n");
        ProgramHead->print(0);
        printf("========AST End========\n");
    }

    if(compileOk)
    {
        m_codeGenerator->generateCode();

        if(m_codeGenerator->isCompileError())
            compileOk = false;
        else
            m_codeGenerator->runCode();
    }

    return compileOk;
}

void yyerror(const char* s){
	fflush(stdout);
	printf("\n%*s\n%*s\n", column, "^", column, s);
}
