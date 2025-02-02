%{
	#include "vwsl_ast.h"
	#include "vwsl_token.h"
	int yylex(void);
	void yyerror(const char *);
	extern char yytext[];
	SegmentAST* ProgramHead;
%}

%debug
%error-verbose

//to avoid if-else conflicts warning message
%nonassoc IFX
%nonassoc ELSE

%nonassoc OP_NE OP_EQ

%union
{
    vwslToken token;
    class NodeAST* node;
    class BlockAST* block;
    class InitDeclListAST* initDecl;
    class InitListAST* init;
    class FunctionAST* func;
    class StringList* arglist;
    class SegmentAST* segment;
    class DeclarationAST* decl;
    class ParaDeclListAST* paraDecl;
    class ParaDeclAST* para;
}

/* literal tokens */
%token <token> IDENTIFIER STRING_LITERAL SIZEOF
%token <token> HEX STRING UNS32 UNS64 FLOAT32 FLOAT64

/* unary expression tokens */
%token <token> PTR_OP        // ->
%token <token> INC_OP        // ++
%token <token> DEC_OP        // --
%token <token> LEFT_OP       // <<
%token <token> RIGHT_OP      // >>

/* assign expression tokens */
%token <token> MUL_ASSIGN    // *=
%token <token> DIV_ASSIGN    // /=
%token <token> MOD_ASSIGN    // %=
%token <token> ADD_ASSIGN    // +=
%token <token> SUB_ASSIGN    // -=
%token <token> LEFT_ASSIGN   // <<=
%token <token> RIGHT_ASSIGN  // >>=
%token <token> AND_ASSIGN    // &=
%token <token> XOR_ASSIGN    // ^=
%token <token> OR_ASSIGN     // |=
%token <token> CAT_ASSIGN    // ~=
%token <token> TYPE_NAME    

/* operator */
%token <token> OP_OR         // ||
%token <token> OP_AND        // &&
%token <token> OP_LT         // >
%token <token> OP_ST         // <
%token <token> OP_LE         // >=
%token <token> OP_SE         // <=
%token <token> OP_NE		 // !=
%token <token> OP_EQ		 // ==
%token <token> SEMICOLON     // ';'
%token <token> LCURLY;       // '{' or '<%'
%token <token> RCURLY;       // '}' or '%>'
%token <token> COMMA;        // ','
%token <token> COLON;        // ':'
%token <token> ASSIGN;       // '='
%token <token> LPAREN;       // '('
%token <token> RPAREN;       // ')'
%token <token> LBRACKET;     // '[' or '<:'
%token <token> RBRACKET;     // ']' or ':>'
%token <token> DOT;          // '.'
%token <token> AND;          // '&'
%token <token> NOT;          // '!'
%token <token> CAT;          // '~'
%token <token> MINUS;        // '-'
%token <token> PLUS;         // '+'
%token <token> MUL;          // '*'
%token <token> DIV;          // '/'
%token <token> MOD;          // '%'
%token <token> XOR;          // '^'
%token <token> OR;           // '|'
%token <token> QUESTION;     // '?'

/* reserved keywords */
%token <token> TYPEDEF EXTERN STATIC AUTO

/* data type */
%token <token> CHAR SHORT INT LONG SIGNED UNSIGNED FLOAT DOUBLE CONST VOLATILE VOID
%token <token> CASE DEFAULT IF SWITCH WHILE DO FOR GOTO CONTINUE BREAK RETURN END
%token <token> NEW PROC CLASS

/* special tokens */
%token <token> STRUCT UNION ENUM ELLIPSIS

%type <block> statement_list block
%type <segment> translation_unit
%type <node> external_declaration class_definition function_definition declaration init_declarator initializer
%type <decl> declaration_specifiers 
%type <token> storage_class_specifier type_specifier type_qualifier assignment_operator
%type <initDecl> init_declarator_list
%type <init> initializer_list
%type <paraDecl> parameter_type_list parameter_list
%type <para> parameter_declaration
%type <func> declarator direct_declarator pointer
%type <arglist> identifier_list

%type <node> statement compound_statement declaration_statement expression_statement selection_statement iteration_statement jump_statement
%type <node> primary_expression expression constant_expression
%type <node> postfix_expression unary_expression multiplicative_expression additive_expression shift_expression cast_expression
%type <node> relational_expression equality_expression and_expression exclusive_or_expression inclusive_or_expression
%type <node> logical_and_expression logical_or_expression conditional_expression assignment_expression
%type <node> constant numeric string

%start translation_unit
%%

primary_expression
	: IDENTIFIER 																			{ $$ = new GetVarAST(NULL, $1.m_name, GetVarAST::NONE); }
	| constant 																				{ $$ = $1; }
	| LPAREN expression RPAREN																{ $$ = $2; }
	;

constant
	: numeric																				{ $$ = $1; }
	| string
	;

numeric
	: UNS32 																				{ $$ = new Int32AST($1.m_integerValue); }
	| UNS64 																				{ $$ = new Int64AST($1.m_integerValue); }
	| FLOAT32 																				{ $$ = new FloatAST($1.m_doubleValue); }
	| FLOAT64 																				{ $$ = new DoubleAST($1.m_doubleValue); }
	;

string
	: STRING_LITERAL 																		{} // L"test"
	| STRING 																				{} // L'test'
	| HEX 																					{} // 0xFFFFFFFF
	;

statement
	: compound_statement 																	{ $$ = $1; }
	| declaration_statement 																{ $$ = $1; }
	| expression_statement 																	{ $$ = $1; }
	| selection_statement 																	{ $$ = $1; }
	| iteration_statement 																	{ $$ = $1; }
	| jump_statement 																		{ $$ = $1; }
	;

statement_list
	: statement 																			{ $$ = new BlockAST(); $$->m_statements.push_back($1); }
	| statement_list statement 																{ if($2 != NULL) $1->m_statements.push_back($2); }
	;

declarator
	: pointer direct_declarator               												{ $$ = $1; }
	| direct_declarator																		{ $$ = $1; }
	;

direct_declarator
	: IDENTIFIER 																			{ $$ = new FunctionAST(*($1.m_name), FunctionAST::e_Identifier); }
	| LPAREN declarator RPAREN																{ $$ = $2; } // TODO
	| direct_declarator LBRACKET constant_expression RBRACKET								{ $1->expr = $3; $1->e_Type = FunctionAST::e_Array; } // test[a<<3]
	| direct_declarator LBRACKET RBRACKET 						     						{ $1->expr = NULL; $1->e_Type = FunctionAST::e_Array; } // test[]
	| direct_declarator LPAREN parameter_type_list RPAREN		    						{ $1->e_Type = FunctionAST::e_Func; $1->parameters = $3; } // test(int a, int b, ...)
	| direct_declarator LPAREN identifier_list RPAREN			    						{ $1->e_Type = FunctionAST::e_Func; $1->Args = &($3->m_stringVec); } // test(a, b, c)
	| direct_declarator LPAREN RPAREN							    						{ $1->e_Type = FunctionAST::e_Func; $1->Args = NULL; } // test()
	;

constant_expression
	: conditional_expression																{}
	;

parameter_type_list
	: parameter_list 																		{ $$ = $1; }
	| parameter_list COMMA ELLIPSIS 														{ $$ = $1; $$->ellipsis_exist = true; }
	;

parameter_list
	: parameter_declaration 																{ $$ = new ParaDeclListAST(); $$->para_decl_list.push_back($1); }
	| parameter_list COMMA parameter_declaration     										{ $1->para_decl_list.push_back($3); }
	;

parameter_declaration
	: declaration_specifiers declarator    													{ $$ = new ParaDeclAST($1, $2); }
	//| declaration_specifiers abstract_declarator 											{}
	| declaration_specifiers 																{ $$ = new ParaDeclAST($1, NULL); }
	;

identifier_list
	: IDENTIFIER 																			{ $$ = new StringList(); $$->m_stringVec.push_back(*($1.m_name)); }
	| identifier_list COMMA IDENTIFIER 														{ $1->m_stringVec.push_back(*($3.m_name)); }
	;

pointer
	: MUL 																					{}
	| MUL type_qualifier_list 																{}
	| MUL pointer																			{}
	//| MUL type_qualifier_list pointer
	;	

declaration
	: declaration_specifiers SEMICOLON 														{ $$ = $1; }
	| declaration_specifiers init_declarator_list SEMICOLON 								{ $1->InitDeclList = $2; $$ = $1; }
	;

declaration_specifiers
	: storage_class_specifier 																{ $$ = new DeclarationAST(); $$->specifiers.push_front($1.m_name); }
	| storage_class_specifier declaration_specifiers 										{ $2->specifiers.push_front($1.m_name); $$ = $2; }
	| type_specifier 																		{ $$ = new DeclarationAST(); $$->specifiers.push_front($1.m_name); }
	| type_specifier declaration_specifiers 												{ $2->specifiers.push_front($1.m_name); $$ = $2; }
	| type_qualifier 																		{ $$ = new DeclarationAST(); $$->specifiers.push_front($1.m_name); }
	| type_qualifier declaration_specifiers 												{ $2->specifiers.push_front($1.m_name); $$ = $2; }
	;

init_declarator_list
	: init_declarator 																		{ $$ = new InitDeclListAST(); $$->decl_inits.push_back($1); }
	| init_declarator_list COMMA init_declarator 											{ $1->decl_inits.push_back($3); }
	;

init_declarator
	: declarator 																			{ $$ = new InitDeclAST($1); }
	| declarator ASSIGN initializer    														{ $$ = new InitDeclAST($1); $1->initializer = $3; }
	;

initializer
	: assignment_expression 																{ $$ = new InitAST($1, NULL, InitAST::e_Expr); }
	| LCURLY initializer_list RCURLY 														{ $$ = new InitAST(NULL, $2, InitAST::e_List); }
	| LCURLY initializer_list COMMA RCURLY	   												{ $$ = new InitAST(NULL, $2, InitAST::e_List_With_Comma); }
	;

initializer_list
	: initializer 																			{ $$ = new InitListAST(); $$->inits.push_back($1); }
	| initializer_list COMMA initializer 													{ $1->inits.push_back($3); }
	;

storage_class_specifier
	: TYPEDEF	   																			{ $$ = $1; }
	| EXTERN 																				{ $$ = $1; }
	| STATIC 																				{ $$ = $1; }
	| AUTO 																					{ $$ = $1; }
	; 

type_specifier
	: VOID
	| CLASS IDENTIFIER                              										{ $$ = $1; } // class class_name variable
	| CHAR 																					{ $$ = $1; }
	| SHORT 																				{ $$ = $1; }
	| INT 																					{ $$ = $1; }
	| LONG 																					{ $$ = $1; }
	| FLOAT 																				{ $$ = $1; }
	| DOUBLE 																				{ $$ = $1; }
	| SIGNED 																				{ $$ = $1; }
	| UNSIGNED 																				{ $$ = $1; }
	//| struct_or_union_specifier 															{ $$ = $1; }
	//| enum_specifier 																		{ $$ = $1; }
	//| TYPE_NAME 																			{ $$ = $1; }
	;

type_qualifier
	: CONST 																				{ $$ = $1; }
	| VOLATILE 																				{ $$ = $1; }
	;

type_qualifier_list
	: type_qualifier 																		{}
	| type_qualifier_list type_qualifier 													{}
	;

compound_statement
	: LCURLY RCURLY																			{ $$ = new BlockAST(); }
	| LCURLY statement_list RCURLY															{ $$ = $2; } // declararion and statement can be everywhere
	//| LCURLY declaration_list RCURLY														{}
	//| LCURLY declaration_list statement_list RCURLY										{}
	;

declaration_statement
	: declaration 																			{ $$ = $1; }
	;

//declaration_list
	//: declaration																			{}
	//| declaration_list declaration														{}
	//;

expression_statement
	: SEMICOLON 																			{ $$ = NULL; }
	| expression SEMICOLON 																	{ $$ = $1; }
	;

selection_statement
	: IF LPAREN expression RPAREN statement ELSE statement 									{ $$ = new IfElseAST($3, $5, $7); }
	| IF LPAREN expression RPAREN statement %prec IFX 										{ $$ = new IfElseAST($3, $5, NULL); }
	//| SWITCH LPAREN expression RPAREN statement 											{ $$ = new SwitchAST($3, $5); }
	;

iteration_statement
	: WHILE LPAREN expression RPAREN statement 												{ $$ = new WhileAST($3, $5); }
	| DO statement WHILE LPAREN expression RPAREN SEMICOLON									{ $$ = new DoWhileAST($5, $2); }
	| postfix_expression DO OR identifier_list OR block END 								{ $$ = new ForAST($4, $1, NULL, $6); } // a.each do |b| decls stmts end
	| FOR LPAREN expression_statement expression_statement RPAREN statement 				{ $$ = new ForAST($3, $4, NULL, $6); }
	| FOR LPAREN expression_statement expression_statement expression RPAREN statement 		{ $$ = new ForAST($3, $4, $5, $7); }
	| FOR LPAREN declaration expression_statement expression RPAREN statement 				{ $$ = new ForAST($3, $4, $5, $7); }
	;

block																						// ruby-like do-end block
	: statement_list 																		{ $$ = $1; }
	;

jump_statement
	: GOTO IDENTIFIER SEMICOLON	 															{}
	| CONTINUE SEMICOLON 																	{ $$ = new ContinueAST(); }
	| BREAK SEMICOLON 																		{ $$ = new BreakAST(); }
	| RETURN SEMICOLON 																		{ $$ = new ReturnAST(NULL); }
	| RETURN expression SEMICOLON 															{ $$ = new ReturnAST($2); }
	;

expression
	: assignment_expression																	{ $$ = $1; }
	//| expression COMMA assignment_expression 												{}
	;

assignment_expression
	: conditional_expression 																{ $$ = $1; }
	| unary_expression assignment_operator assignment_expression							{ $$ = new AssignmentAST($1, $2.m_name, $3); } // (++b) += 3
	| procedure_expression																	{} //{ $$ = new AssignmentAST($1, $2.m_name, $3); } // test = Proc.new {}
	;

procedure_expression
	: PROC DOT NEW compound_statement 														{}
	;

assignment_operator
	: ASSIGN
	| MUL_ASSIGN
	| DIV_ASSIGN
	| MOD_ASSIGN
	| ADD_ASSIGN
	| SUB_ASSIGN
	| LEFT_ASSIGN
	| RIGHT_ASSIGN
	| AND_ASSIGN
	| XOR_ASSIGN
	| OR_ASSIGN
	;

conditional_expression
	: logical_or_expression 																{ $$ = $1; }
	| logical_or_expression QUESTION expression COLON conditional_expression 				{ $$ = new ConditionalAST($1, $3, $5); }
	;

logical_or_expression
	: logical_and_expression 																{ $$ = $1; }
	| logical_or_expression OP_OR logical_and_expression									{ $$ = new LogicalOrAST($1, $3); }
	;

logical_and_expression
	: inclusive_or_expression 																{ $$ = $1; }
	| logical_and_expression OP_AND inclusive_or_expression									{ $$ = new LogicalAndAST($1, $3); }
	;

inclusive_or_expression
	: exclusive_or_expression 																{ $$ = $1; }
	| inclusive_or_expression OR exclusive_or_expression									{ $$ = new InclusiveOrAST($1, $3); }
	;

exclusive_or_expression
	: and_expression 																		{ $$ = $1; }
	| exclusive_or_expression XOR and_expression 											{ $$ = new ExclusiveOrAST($1, $3); }
	;

and_expression
	: equality_expression 																	{ $$ = $1; }
	| and_expression AND equality_expression  												{ $$ = new AndAST($1, $3); }
	;

equality_expression
	: relational_expression 																{ $$ = $1; }
	| equality_expression OP_EQ relational_expression										{ $$ = new EqualityAST($1, $2.m_name, $3); }
	| equality_expression OP_NE relational_expression                   					{ $$ = new EqualityAST($1, $2.m_name, $3); }
	;

relational_expression
	: shift_expression 																		{ $$ = $1; }
	| relational_expression OP_ST shift_expression 											{ $$ = new RelationalAST($1, $2.m_name, $3); }
	| relational_expression OP_LT shift_expression 											{ $$ = new RelationalAST($1, $2.m_name, $3); }
	| relational_expression OP_SE shift_expression 											{ $$ = new RelationalAST($1, $2.m_name, $3); }
	| relational_expression OP_LE shift_expression 											{ $$ = new RelationalAST($1, $2.m_name, $3); }
	;

shift_expression
	: additive_expression																	{ $$ = $1; }
	| shift_expression LEFT_OP additive_expression 											{ $$ = new ShiftAST($1, $2.m_name, $3); }
	| shift_expression RIGHT_OP additive_expression                     					{ $$ = new ShiftAST($1, $2.m_name, $3); }
	;

additive_expression
	: multiplicative_expression																{ $$ = $1; }
	| additive_expression PLUS multiplicative_expression									{ $$ = new AdditiveAST($1, $2.m_name, $3); }
	| additive_expression MINUS multiplicative_expression									{ $$ = new AdditiveAST($1, $2.m_name, $3); }
	;

multiplicative_expression
	: cast_expression																		{ $$ = $1; }
	| multiplicative_expression MUL cast_expression                     					{ $$ = new MultiplicativeAST($1, $2.m_name, $3); }
	| multiplicative_expression DIV cast_expression                    						{ $$ = new MultiplicativeAST($1, $2.m_name, $3); }
	| multiplicative_expression MOD cast_expression                     					{ $$ = new MultiplicativeAST($1, $2.m_name, $3); }
	;

cast_expression
	: unary_expression 																		{ $$ = $1; }
	| LPAREN type_name RPAREN cast_expression 												{}
	;

type_name
	: specifier_qualifier_list 																{}
	;

specifier_qualifier_list
	: type_specifier specifier_qualifier_list 												{}
	| type_specifier 																		{}
	| type_qualifier specifier_qualifier_list 												{}
	| type_qualifier 																		{}
	;

unary_expression
	: postfix_expression 																	{ $$ = $1; }
	| INC_OP unary_expression 																{ $$ = new UnaryAST($2, $1.m_name); }
	| DEC_OP unary_expression 																{ $$ = new UnaryAST($2, $1.m_name); }
	| unary_operator cast_expression 														{}
	| SIZEOF unary_expression 																{}
	| SIZEOF LPAREN type_name RPAREN 														{}
	;

unary_operator
	: AND
	| MUL
	| PLUS
	| MINUS
	| CAT
	| NOT
	;

postfix_expression
	: primary_expression 																	{ $$ = $1; }
	| postfix_expression LBRACKET expression RBRACKET 										{}
	| postfix_expression LPAREN RPAREN 														{}
	| postfix_expression LPAREN argument_expression_list RPAREN								{} // test(a, b), when in a function and want to call another one
	| postfix_expression DOT IDENTIFIER 													{ $$ = new GetVarAST($1, $3.m_name, GetVarAST::NONE); }
	| postfix_expression PTR_OP IDENTIFIER 													{}
	| postfix_expression INC_OP 															{ $$ = new PostfixIncrementAST($1, $2.m_name); }
	| postfix_expression DEC_OP 															{ $$ = new PostfixIncrementAST($1, $2.m_name); }
	;

argument_expression_list
	: assignment_expression 																{}
	| argument_expression_list COMMA assignment_expression 									{}
	;

translation_unit
	: external_declaration 																	{ ProgramHead = new SegmentAST(); $$ = ProgramHead; if($1 != NULL) $$->m_segments.push_back($1); }
	| translation_unit external_declaration 												{ if($2 != NULL) $1->m_segments.push_back($2); }
	;

external_declaration
	: class_definition 																		{ $$ = $1; }
	| function_definition 																	{ $$ = $1; }
	| declaration 																			{ $$ = $1; }
	;

class_definition
	: CLASS IDENTIFIER compound_statement										 			{} // class test {}
	| CLASS IDENTIFIER OP_ST IDENTIFIER compound_statement						 			{} // class test < test2 {}
	;

function_definition
	: declaration_specifiers declarator compound_statement						 			{ $$ = new DefinitionAST($1, $2, $3, DefinitionAST::e_Func); } // int test(int a, inr b) {}
	| declarator compound_statement												 			{} // test(a, b) {} , which is a definition
	;

%%