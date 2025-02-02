/* see copyright notice in VWSLang.h */

#ifndef VWSL_AST_H
#define VWSL_AST_H

#include "VWSLang.h"
#include <vector>
#include <deque>
#include <map>
#include <stdio.h>
#include <string>

using namespace std;

typedef vector<string> StringVec;
class CodeGenerator;

class NodeAST // AST NODE
{
public:
    enum NodeASTType
    {
        tNodeAST, // type of NodeAST
        tNodeASTList,
        tStringList,
        tParaDeclAST,
        tParaDeclListAST,
        tInitDeclAST,
        tInitDeclListAST,
        tInitAST,
        tInitListAST,
        tNullAST,
        tBooleanAST,
        tInt32AST,
        tInt64AST, // LONG
        tFloatAST,
        tDoubleAST,
        tDeclarationAST,
        tDefinitionAST,
        tGetVarAST,
        tFunctionAST,
        tReturnAST,
        tSegmentAST,
        tBlockAST,
        tPostfixIncrementAST,
        tUnaryAST,
        tMultiplicativeAST,
        tAdditiveAST,
        tShiftAST,
        tRelationalAST,
        tEqualityAST,
        tAndAST,
        tExclusiveOrAST,
        tInclusiveOrAST,
        tLogicalAndAST,
        tLogicalOrAST,
        tConditionalAST,
        tAssignmentAST,
        tIfElseAST,
        tSwitchAST,
        tCaseAST,
        tContinueAST,
        tBreakAST,
        tWhileAST,
        tDoWhileAST,
        tForAST,
        tNewAST,
        tDeleteAST
    };
    NodeASTType m_type;
    int m_line; //for debuginfo

    NodeAST() : m_type(tNodeAST), m_line(0) {}
    virtual ~NodeAST() {}
    virtual void print(int indent) = 0;
    void printIndent(int indent)
    {
        while(indent-- > 0)
        {
            printf(" ");
        }
    }
};

typedef vector<NodeAST*> NodeASTVec;

class NodeASTList : public NodeAST
{
public:
    NodeASTVec m_nodeASTVec;

    NodeASTList()
    {
        m_type = tNodeASTList;
    }
    virtual ~NodeASTList()
    {
        m_nodeASTVec.clear();
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[NodeASTList]:\n");
        NodeASTVec::iterator it = m_nodeASTVec.begin();
        for(; it != m_nodeASTVec.end(); ++it)
        {
            (*it)->print(indent+2);
        }
    }
    void push_back(NodeAST* node) { m_nodeASTVec.push_back(node); }
};

class StringList : public NodeAST
{
public:
    StringVec m_stringVec;
    bool m_hasDuplicateStr;

    StringList()
    {
        m_type = tStringList;
        m_hasDuplicateStr = false;
    }
    virtual ~StringList()
    {
        m_stringVec.clear();
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[StringList]:\n");
        StringVec::iterator it = m_stringVec.begin();
        for(; it != m_stringVec.end(); ++it)
        {
            printIndent(indent+2);
            printf("%s\n", it->c_str());
        }
    }
    void push_back(const std::string& str)
    {
        for(size_t i = 0; i < m_stringVec.size(); ++i)
        {
            if(str.compare(m_stringVec[i]) == 0)
            {
                m_hasDuplicateStr = true;
                break;
            }
        }
        m_stringVec.push_back(str);
    }

};

class ParaDeclAST : public NodeAST
{
public:
    NodeAST* decl = NULL;
    NodeAST* func = NULL;
    ParaDeclAST(NodeAST* d, NodeAST* f)
        : decl(d)
        , func(f)
    {
        m_type = tParaDeclAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ParaDeclAST]\n");
        if(decl)
        {
            decl->print(indent+2);
        }
        if(func)
        {
            func->print(indent+2);
        }
    }
};

class ParaDeclListAST : public NodeAST
{
public:
    vector<NodeAST*> para_decl_list;
    bool ellipsis_exist;
    ParaDeclListAST()
    {
        m_type = tParaDeclListAST;
        ellipsis_exist = false;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ParaDeclListAST]\n");
        vector<class NodeAST*>::iterator it = para_decl_list.begin();
        for(int i = 0; it != para_decl_list.end(); ++it, ++i)
        {
            printIndent(indent+2);
            printf("para_decl[%d]:\n", i);
            (*it)->print(indent+2);
        }
        if(ellipsis_exist)
        {
            printIndent(indent+2);
            printf("...\n");
        }
    }
};

class InitDeclAST : public NodeAST
{
public:
    NodeAST* func = NULL;
    InitDeclAST(NodeAST* f)
        : func(f)
    {
        m_type = tInitDeclAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[InitDeclAST]\n");
        if(func)
        {
            func->print(indent+2);
        }
    }
};

class InitDeclListAST : public NodeAST
{
public:
    vector<NodeAST*> decl_inits;
    InitDeclListAST()
    {
        m_type = tInitDeclListAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[InitDeclListAST]\n");
        vector<NodeAST*>::iterator it = decl_inits.begin();
        for(int i = 0; it != decl_inits.end(); ++it, ++i)
        {
            printIndent(indent+2);
            printf("decl_inits[%d]:\n", i);
            (*it)->print(indent+2);
        }
    }
};

class InitAST : public NodeAST
{
public:
    NodeAST* expr;
    NodeAST* init_list;
    enum type
    {
        e_Expr = 1,
        e_List,
        e_List_With_Comma
    }e_Type;
    InitAST(NodeAST* e, NodeAST* i, enum type e_type)
        : expr(e)
        , init_list(i)
        , e_Type(e_type)
    {
        m_type = tInitAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[InitAST]\n");
        if(e_Type == 1)
        {
            expr->print(indent+2);
        }
        else if(e_Type == 2)
        {
            init_list->print(indent+2);
        }
        else
        {
            init_list->print(indent+2);
        }
    }
};

class InitListAST : public NodeAST
{
public:
    NodeASTVec inits;
    InitListAST()
    {
        m_type = tInitListAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[InitListAST]\n");
        NodeASTVec::iterator it = inits.begin();
        for(int i = 0; it != inits.end(); ++it, ++i)
        {
            printIndent(indent+2);
            printf("inits[%d]:\n", i);
            (*it)->print(indent+2);
        }
    }
};

class NullAST : public NodeAST
{
public:
    NullAST()
    {
        m_type = tNullAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[NullAST]\n");
    }
};

class BooleanAST : public NodeAST
{
public:
    int m_val;
    BooleanAST(int val)
        : m_val(val)
    {
        m_type = tBooleanAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[BooleanAST]: m_val=%d\n", m_val);
    }
};

class Int32AST : public NodeAST
{
public:
    vwslUInt32 m_val;
    Int32AST(vwslUInt32 val)
        : m_val(val)
    {
        m_type = tInt32AST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[Int32AST]: m_val=%d\n", m_val);
    }
};

class Int64AST : public NodeAST
{
public:
    vwslUInt64 m_val;
    Int64AST(vwslUInt64 val)
        : m_val(val)
    {
        m_type = tInt64AST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[Int64AST]: m_val=%lu\n", m_val);
    }
};

class FloatAST : public NodeAST
{
public:
    vwslFloat m_val;
    FloatAST(vwslFloat val)
        : m_val(val)
    {
        m_type = tFloatAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[FloatAST]: m_val=%f\n", m_val);
    }
};

class DoubleAST : public NodeAST
{
public:
    vwslDouble m_val;
    DoubleAST(vwslDouble val)
        : m_val(val)
    {
        m_type = tDoubleAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[DoubleAST]: m_val=%lf\n", m_val);
    }
};

class DeclarationAST : public NodeAST
{
public:
    deque<string*> specifiers;
    class InitDeclListAST* InitDeclList = NULL;
    DeclarationAST()
    {
        m_type = tDeclarationAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[DeclarationAST]\n");
        printIndent(indent);
        printf("Specifier(s): ");
        deque<string*>::iterator it = specifiers.begin();
        for(int i = 0; it != specifiers.end(); ++it, ++i)
        {
            printf("%s ", (*it)->c_str());
        }
        printf("\n");
        if(InitDeclList)
        {       
            InitDeclList->print(indent+2);
        }
    }
};

class DefinitionAST : public NodeAST
{
public:
    NodeAST* decl_spec;
    NodeAST* declarator;
    NodeAST* block;
    enum type
    {
        e_Class = 1,
        e_Func
    }e_Type;
    DefinitionAST(NodeAST* ds, NodeAST* d, NodeAST* b, enum type e_type)
        : decl_spec(ds)
        , declarator(d)
        , block(b)
        , e_Type(e_type)
    {
        m_type = tDefinitionAST;
    }
    virtual void print(int indent)
    {
        if(e_Type == 1)
        {
            printIndent(indent);
            printf("[DefinitionAST[1] || e_Class]:\n");
        }
        else if(e_Type == 2)
        {
            printIndent(indent);
            printf("[DefinitionAST[2] || e_Func]:\n");
            decl_spec->print(indent+2);
            declarator->print(indent+2);
            block->print(indent+2);
        }
    }
};

//key or table.key or table[key] or array[index]
class GetVarAST : public NodeAST
{
public:
    NodeAST* m_parentExpr;
    string* m_keyIdentifier;
    NodeAST* m_keyExpr;
    enum Scope
    {
        NONE,   //not specified
        GLOBAL, //::key
        THIS,   //this.key or this[key]
    };
    Scope m_scope;
    int m_isFuncCall;
    
    GetVarAST(NodeAST* parentExpr, string* key, Scope scope = NONE)
        : m_parentExpr(parentExpr)
        , m_keyIdentifier(key)
        , m_keyExpr(0)
        , m_scope(scope)
        , m_isFuncCall(0)
    {
        m_type = tGetVarAST;
    }

    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[GetVarAST]:\n");

        printIndent(indent+2);
        printf("m_parentExpr:\n");
        if(m_parentExpr != 0) m_parentExpr->print(indent+2);

        printIndent(indent+2);
        printf("m_scope: ");
        switch(m_scope)
        {
            case NONE: printf("NONE\n"); break;
            case GLOBAL: printf("GLOBAL\n"); break;
            case THIS: printf("THIS\n"); break;
        }

        printIndent(indent+2);
        if(!m_keyIdentifier->empty())
        {
            printf("m_keyIdentifier: %s\n", m_keyIdentifier->c_str());
        }
        else
        {
            printf("m_keyExpr:\n");
            m_keyExpr->print(indent+2);
        }
    }
};

class FunctionAST : public NodeAST
{
public:
    string Name;
    vector<string>* Args;
    enum type
    {
        e_Identifier = 1,
        e_Array,
        e_Func
    } e_Type;
    NodeAST* expr = NULL; 
    NodeAST* initializer = NULL;
    NodeAST* parameters = NULL;

    FunctionAST(const string &name, enum type e_type)
        : Name(name)
        , e_Type(e_type)
    {
        m_type = tFunctionAST;
    }
    virtual void print(int indent)
    {
        if(e_Type == 1)
        {
            printIndent(indent);
            printf("[FunctionAST[1] || e_Identifier]:\n");
            printIndent(indent);
            printf("Name: %s\n", Name.c_str());
            printIndent(indent);
            printf("Initializer:\n");
            if(initializer)
            {
                initializer->print(indent+2);
            }
            else
            {
                printIndent(indent);
                printf("Empty\n");
            }
        }
        else if(e_Type == 2)
        {
            printIndent(indent);
            printf("[FunctionAST[2] || e_Array]:\n");
            printIndent(indent);
            printf("Name: %s\n", Name.c_str());
            printIndent(indent);
            printf("Amount:\n");
            if(expr)
            {
                expr->print(indent+2);
            }
            else
            {
                printIndent(indent);
                printf("Empty\n");
            }
            printIndent(indent);
            printf("Initializer:\n");
            if(initializer)
            {
                initializer->print(indent+2);
            }
            else
            {
                printIndent(indent);
                printf("Empty\n");
            }
        }
        else
        {
            printIndent(indent);
            printf("[FunctionAST[3] || e_Func]:\n");
            printIndent(indent);
            printf("Name: %s\n", Name.c_str());
            if((Args != NULL) && (Args->size() != 0))
            {
                printIndent(indent);
                printf("identifier_list: ");
                for(vector<string>::iterator it = Args->begin(); it != Args->end(); ++it)
                {
                    printf("%s ", it->c_str());
                }
                printf("\n");
            }
            else if(parameters)
            {
                printIndent(indent);
                printf("parameter_list:\n");
                parameters->print(indent+2);
            }
        }
    }
};

class ReturnAST : public NodeAST
{
public:
    NodeAST* m_expr = NULL;
    ReturnAST(NodeAST* expr)
        : m_expr(expr)
    {
        m_type = tReturnAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ReturnAST]:\n");
        if(m_expr)
        {
            printIndent(indent+2);
            printf("m_expr:\n");
            m_expr->print(indent+2);
        }
        else
        {
            printIndent(indent+2);
            printf("return;\n");
        }
    }
};

class SegmentAST : public NodeAST
{
public:
    NodeASTVec m_segments;
    SegmentAST()
    {
        m_type = tSegmentAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[SegmentAST]:\n");
        NodeASTVec::iterator it = m_segments.begin();
        for(int i = 0; it != m_segments.end(); ++it, ++i)
        {
            printIndent(indent+2);
            printf("m_segments[%d]:\n", i);
            (*it)->print(indent+2);
        }
    }
};

class BlockAST : public NodeAST
{
public:
    NodeASTVec m_statements;
    BlockAST()
    {
        m_type = tBlockAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[BlockAST]:\n");
        NodeASTVec::iterator it = m_statements.begin();
        for(int i = 0; it != m_statements.end(); ++it, ++i)
        {
            printIndent(indent+2);
            printf("m_statements[%d]:\n", i);
            (*it)->print(indent+2);
        }
    }
};

class PostfixIncrementAST : public NodeAST
{
public:
    NodeAST* m_expr;
    std::string* m_op;
    PostfixIncrementAST(NodeAST* expr, std::string* op)
        : m_expr(expr)
        , m_op(op)
    {
        m_type = tPostfixIncrementAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[PostfixIncrementAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr:\n");
        m_expr->print(indent+2);
    }
};

class UnaryAST : public NodeAST
{
public:
    NodeAST* m_expr;
    std::string* m_op;
    UnaryAST(NodeAST* expr, std::string* op)
        : m_expr(expr)
        , m_op(op)
    {
        m_type = tUnaryAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[UnaryAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr:\n");
        m_expr->print(indent+2);
    }
};

class MultiplicativeAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    std::string* m_op;
    MultiplicativeAST(NodeAST* expr1, std::string* op, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
        , m_op(op)
    {
        m_type = tMultiplicativeAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[MultiplicativeAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class AdditiveAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    std::string* m_op;
    AdditiveAST(NodeAST* expr1, std::string* op, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
        , m_op(op)
    {
        m_type = tAdditiveAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[AdditiveAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class ShiftAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    std::string* m_op;
    ShiftAST(NodeAST* expr1, std::string* op, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
        , m_op(op)
    {
        m_type = tShiftAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ShiftAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class RelationalAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    std::string* m_op;
    RelationalAST(NodeAST* expr1, std::string* op, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
        , m_op(op)
    {
        m_type = tRelationalAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[RelationalAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class EqualityAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    std::string* m_op;
    EqualityAST(NodeAST* expr1, std::string* op, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
        , m_op(op)
    {
        m_type = tEqualityAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[EqualityAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class AndAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    AndAST(NodeAST* expr1, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
    {
        m_type = tAndAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[AndAST]: m_op: &\n");
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class ExclusiveOrAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    ExclusiveOrAST(NodeAST* expr1, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
    {
        m_type = tExclusiveOrAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ExclusiveOrAST]: m_op: ^\n");
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class InclusiveOrAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    InclusiveOrAST(NodeAST* expr1, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
    {
        m_type = tInclusiveOrAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[InclusiveOrAST]: m_op: |\n");
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class LogicalAndAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    LogicalAndAST(NodeAST* expr1, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
    {
        m_type = tLogicalAndAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[LogicalAndAST]: m_op: &&\n");
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class LogicalOrAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    LogicalOrAST(NodeAST* expr1, NodeAST* expr2)
        : m_expr1(expr1)
        , m_expr2(expr2)
    {
        m_type = tLogicalOrAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[LogicalAndAST]: m_op: ||\n");
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class ConditionalAST : public NodeAST
{
public:
    NodeAST* m_operand1;
    NodeAST* m_operand2;
    NodeAST* m_operand3;
    ConditionalAST(NodeAST* oprd1, NodeAST* oprd2, NodeAST* oprd3)
        : m_operand1(oprd1)
        , m_operand2(oprd2)
        , m_operand3(oprd3)
    {
        m_type = tConditionalAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ConditionalAST]: m_op: ?:\n");
        printIndent(indent+2);
        printf("m_operand1:\n");
        m_operand1->print(indent+2);
        printIndent(indent+2);
        printf("m_operand2:\n");
        m_operand2->print(indent+2);
        printIndent(indent+2);
        printf("m_operand3:\n");
        m_operand3->print(indent+2);
    }
};

class AssignmentAST : public NodeAST
{
public:
    NodeAST* m_expr1;
    NodeAST* m_expr2;
    std::string* m_op;
    AssignmentAST(NodeAST* lhs, std::string* op, NodeAST* rhs)
        : m_expr1(lhs)
        , m_expr2(rhs)
        , m_op(op)
    {
        m_type = tAssignmentAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[AssignmentAST]: m_op: %s\n", m_op->c_str());
        printIndent(indent+2);
        printf("m_expr1:\n");
        m_expr1->print(indent+2);
        printIndent(indent+2);
        printf("m_expr2:\n");
        m_expr2->print(indent+2);
    }
};

class IfElseAST : public NodeAST
{
public:
    NodeAST* m_cond;
    NodeAST* m_thenStmt;
    NodeAST* m_elseStmt = NULL;
    IfElseAST(NodeAST* cond, NodeAST* thenStmt, NodeAST* elseStmt)
        : m_cond(cond)
        , m_thenStmt(thenStmt)
        , m_elseStmt(elseStmt)
    {
        m_type = tIfElseAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[IfElseAST]:\n");
        printIndent(indent+2);
        printf("m_cond:\n");
        m_cond->print(indent+2);
        printIndent(indent+2);
        printf("m_thenStmt:\n");
        m_thenStmt->print(indent+2);
        if(m_elseStmt)
        {
            printIndent(indent+2);
            printf("m_elseStmt:\n");
            m_elseStmt->print(indent+2);
        }
    }
};

class SwitchAST : public NodeAST
{
public:
    NodeAST* m_cond;
    NodeASTList* m_labeledStmts;
    SwitchAST(NodeAST* cond, NodeASTList* labeledStmts)
        : m_cond(cond)
        , m_labeledStmts(labeledStmts)
    {
        m_type = tSwitchAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[SwitchAST]:\n");
        printIndent(indent+2);
        printf("m_cond:\n");
        m_cond->print(indent+2);
        printIndent(indent+2);
        printf("m_labeledStmts:\n");
        m_labeledStmts->print(indent+2);
    }
};

class CaseAST : public NodeAST
{
public:
    NodeAST* m_expr; //if expr is NULL, this is 'default:' statement.
    BlockAST* m_stmts = NULL;

    CaseAST(NodeAST* expr, BlockAST* stmts)
        : m_expr(expr)
        , m_stmts(stmts)
    {
        m_type = tCaseAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[CaseAST]: %s\n", m_expr==0? "default" : "case");
        if(m_expr)
        {
            printIndent(indent+2);
            printf("m_expr:\n");
            m_expr->print(indent+2);
        }
        printIndent(indent+2);
        if(m_stmts == NULL)
        {
            printf("m_stmts: null\n");
        }
        else
        {
            printf("m_stmts:\n");
            m_stmts->print(indent+2);
        }
    }
};

class ContinueAST : public NodeAST
{
public:
    ContinueAST()
    {
        m_type = tContinueAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ContinueAST]: continue;\n");
    }
};

class BreakAST : public NodeAST
{
public:
    BreakAST()
    {
        m_type = tBreakAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[BreakAST]: break;\n");
    }
};

class WhileAST : public NodeAST
{
public:
    NodeAST* m_cond;
    NodeAST* m_stmt;
    WhileAST(NodeAST* cond, NodeAST* stmt)
        : m_cond(cond)
        , m_stmt(stmt)
    {
        m_type = tWhileAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[WhileAST]:\n");
        printIndent(indent+2);
        printf("m_cond:\n");
        m_cond->print(indent+2);
        printIndent(indent+2);
        printf("m_stmt:\n");
        m_stmt->print(indent+2);
    }
};

class DoWhileAST : public NodeAST
{
public:
    NodeAST* m_cond;
    NodeAST* m_stmt;
    DoWhileAST(NodeAST* cond, NodeAST* stmt)
        : m_cond(cond)
        , m_stmt(stmt)
    {
        m_type = tDoWhileAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[DoWhileAST]:\n");
        printIndent(indent+2);
        printf("m_cond:\n");
        m_cond->print(indent+2);
        printIndent(indent+2);
        printf("m_stmt:\n");
        m_stmt->print(indent+2);
    }
};

class ForAST : public NodeAST
{
public:
    NodeAST* m_init = NULL;
    NodeAST* m_cond = NULL;
    NodeAST* m_inc = NULL;
    NodeAST* m_stmt;
    ForAST(NodeAST* init, NodeAST* cond, NodeAST* inc, NodeAST* stmt)
        : m_init(init)
        , m_cond(cond)
        , m_inc(inc)
        , m_stmt(stmt)
    {
        m_type = tForAST;
    }
    virtual void print(int indent)
    {
        printIndent(indent);
        printf("[ForAST]:\n");
        printIndent(indent+2);
        if(m_init)
        {
            printf("m_init:\n");
            m_init->print(indent+2);
        }
        else
        {
            printf("m_init: NULL\n");
        }
        printIndent(indent+2);
        if(m_cond)
        {
            printf("m_cond:\n");
            m_cond->print(indent+2);
        }
        else
        {
            printf("m_cond: NULL\n");
        }
        printIndent(indent+2);
        if(m_inc)
        {
            printf("m_inc:\n");
            m_inc->print(indent+2);
        }
        else
        {
            printf("m_inc: NULL\n");
        }
        printIndent(indent+2);
        printf("m_stmt:\n");
        m_stmt->print(indent+2);
    }
};

#endif //VWSL_AST_H
