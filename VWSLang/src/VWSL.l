D			[0-9]
L			[a-zA-Z_]
H			[a-fA-F0-9]
E			[Ee][+-]?{D}+
FS			(f|F|l|L)
IS			(u|U|l|L)*

%{
	#include "vwsl_token.h"
	#include <stdio.h>
	#include <stdlib.h>
	#include <string>
	#include "y.tab.h" // from yacc

	typedef int vwslTokenType;

	using namespace std;
	string comment;

	void yyerror(const char *);
	void count();
	int check_type();
%}

/* Reserved words */
VWSLReturn  "return"
VWSLTypedef "typedef"
VWSLSizeof  "sizeof"
VWSLStatic  "static"
VWSLGoto    "goto"
VWSLIf      "if"
VWSLElse    "else"
VWSLConst   "const"
VWSLShort   "short"
VWSLLong    "long"
VWSLInt     "int"
VWSLSigned  "signed"
VWSLUnsigned"unsigned" 
VWSLFloat   "float"
VWSLDouble  "double"
VWSLChar    "char"
VWSLFor     "for"
VWSLStruct  "struct"
VWSLUnion   "union"
VWSLVoid    "void"
VWSLDo      "do"
VWSLWhile   "while"
VWSLSwitch  "switch"
VWSLVolatile "volatile"
VWSLAuto    "auto"
VWSLEnd    "end"

/* procedure */
VWSLNew     "new"
VWSLProc    "Proc"

/* class */
VWSLClass   "class"

/* Relational operators */
OP_le ">="
OP_se "<="
OP_ne "!="
OP_eq "=="

/* Logical operators */
OP_or      "||"
OP_and     "&&"

error    .

%x CMNT
%x M_CMNT

%%
"//"        { comment = "//"; BEGIN CMNT; }
<CMNT>.     { comment.append(1, yytext[0]); }
<CMNT>\n    { BEGIN INITIAL; comment.append(1, '\n'); printf("%s", comment.c_str()); }

"/*"          { comment = "/*"; BEGIN M_CMNT; }
<M_CMNT>.     { comment.append(1, yytext[0]); }
<M_CMNT>\n    { comment.append(1, '\n'); }
<M_CMNT>"*/"  { BEGIN INITIAL; comment.append("*/"); printf("%s", comment.c_str()); }

{VWSLReturn}  { count(); yylval.token.m_type = RETURN; yylval.token.m_name = new string(yytext); return RETURN; }
{VWSLTypedef} { count(); yylval.token.m_type = TYPEDEF; yylval.token.m_name = new string(yytext); return TYPEDEF; }
{VWSLSizeof}  { count(); yylval.token.m_type = SIZEOF; yylval.token.m_name = new string(yytext); return SIZEOF; }
{VWSLStatic}  { count(); yylval.token.m_type = STATIC; yylval.token.m_name = new string(yytext); return STATIC; }
{VWSLGoto}    { count(); yylval.token.m_type = GOTO; yylval.token.m_name = new string(yytext); return GOTO; }
{VWSLIf}      { count(); yylval.token.m_type = IF; yylval.token.m_name = new string(yytext); return IF; }
{VWSLElse}    { count(); yylval.token.m_type = ELSE; yylval.token.m_name = new string(yytext); return ELSE; }
{VWSLConst}   { count(); yylval.token.m_type = CONST; yylval.token.m_name = new string(yytext); return CONST; }
{VWSLShort}   { count(); yylval.token.m_type = SHORT; yylval.token.m_name = new string(yytext); return SHORT; }
{VWSLLong}    { count(); yylval.token.m_type = LONG; yylval.token.m_name = new string(yytext); return LONG; }
{VWSLInt}     { count(); yylval.token.m_type = INT; yylval.token.m_name = new string(yytext); return INT; }
{VWSLSigned}  { count(); yylval.token.m_type = SIGNED; yylval.token.m_name = new string(yytext); return SIGNED; }
{VWSLUnsigned}  { count(); yylval.token.m_type = UNSIGNED; yylval.token.m_name = new string(yytext); return UNSIGNED; }
{VWSLFloat}   { count(); yylval.token.m_type = FLOAT; yylval.token.m_name = new string(yytext); return FLOAT; }
{VWSLDouble}  { count(); yylval.token.m_type = DOUBLE; yylval.token.m_name = new string(yytext); return DOUBLE; }
{VWSLChar}    { count(); yylval.token.m_type = CHAR; yylval.token.m_name = new string(yytext); return CHAR; }
{VWSLFor}     { count(); yylval.token.m_type = FOR; yylval.token.m_name = new string(yytext); return FOR; }
{VWSLStruct}  { count(); yylval.token.m_type = STRUCT; yylval.token.m_name = new string(yytext); return STRUCT; }
{VWSLUnion}   { count(); yylval.token.m_type = UNION; yylval.token.m_name = new string(yytext); return UNION; }
{VWSLVoid}    { count(); yylval.token.m_type = VOID; yylval.token.m_name = new string(yytext); return VOID; }
{VWSLDo}      { count(); yylval.token.m_type = DO; yylval.token.m_name = new string(yytext); return DO; }
{VWSLWhile}   { count(); yylval.token.m_type = WHILE; yylval.token.m_name = new string(yytext); return WHILE; }
{VWSLSwitch}  { count(); yylval.token.m_type = SWITCH; yylval.token.m_name = new string(yytext); return SWITCH; }
{VWSLVolatile} { count(); yylval.token.m_type = VOLATILE; yylval.token.m_name = new string(yytext); return VOLATILE; }
{VWSLAuto}    { count(); yylval.token.m_type = AUTO; yylval.token.m_name = new string(yytext); return AUTO; }
{VWSLEnd}     { count(); yylval.token.m_type = END; yylval.token.m_name = new string(yytext); return END; }

{VWSLNew}     { count(); yylval.token.m_type = NEW; yylval.token.m_name = new string(yytext); return NEW; }
{VWSLProc}    { count(); yylval.token.m_type = PROC; yylval.token.m_name = new string(yytext); return PROC; }
{VWSLClass}   { count(); yylval.token.m_type = CLASS; yylval.token.m_name = new string(yytext); return CLASS; }

{OP_le}    { count(); yylval.token.m_type = OP_LE; yylval.token.m_name = new string(yytext); return OP_LE; }
{OP_se}    { count(); yylval.token.m_type = OP_SE; yylval.token.m_name = new string(yytext); return OP_SE; }
{OP_ne}    { count(); yylval.token.m_type = OP_NE; yylval.token.m_name = new string(yytext); return OP_NE; }
{OP_eq}    { count(); yylval.token.m_type = OP_EQ; yylval.token.m_name = new string(yytext); return OP_EQ; }

{OP_or}    { count(); yylval.token.m_type = OP_OR; yylval.token.m_name = new string(yytext); return OP_OR; }
{OP_and}   { count(); yylval.token.m_type = OP_AND; yylval.token.m_name = new string(yytext); return OP_AND; }

{L}({L}|{D})*		{ count(); yylval.token.m_type = IDENTIFIER; yylval.token.m_name = new string(yytext); return IDENTIFIER; }

0[xX]{H}+{IS}?		{ count(); yylval.token.m_type = HEX; yylval.token.m_name = new string(yytext); return HEX; }
0{D}+{IS}?		    { count(); yylval.token.m_type = UNS32; yylval.token.m_integerValue = atoi(yytext); yylval.token.m_name = new string(yytext); return UNS32; }
{D}+{IS}?		    { count(); yylval.token.m_type = UNS32; yylval.token.m_integerValue = atoi(yytext); yylval.token.m_name = new string(yytext); return UNS32; }
L?'(\\.|[^\\'])+'	{ count(); yylval.token.m_type = STRING; yylval.token.m_name = new string(yytext); return STRING; }

{D}+{E}{FS}?		{ count(); yylval.token.m_type = UNS64; yylval.token.m_name = new string(yytext); return UNS64; }
{D}*"."{D}+({E})?{FS}?	{ count(); yylval.token.m_type = FLOAT32; yylval.token.m_doubleValue = atof(yytext); yylval.token.m_name = new string(yytext); return FLOAT32; }
{D}+"."{D}*({E})?{FS}?	{ count(); yylval.token.m_type = FLOAT64; yylval.token.m_doubleValue = atof(yytext); yylval.token.m_name = new string(yytext); return FLOAT64; }

L?\"(\\.|[^\\"])*\"	{ count(); yylval.token.m_type = STRING_LITERAL; yylval.token.m_name = new string(yytext); return STRING_LITERAL; }

"..."			{ count(); yylval.token.m_type = ELLIPSIS; yylval.token.m_name = new string(yytext); return ELLIPSIS; }
">>="			{ count(); yylval.token.m_type = RIGHT_ASSIGN; yylval.token.m_name = new string(yytext); return RIGHT_ASSIGN; }
"<<="			{ count(); yylval.token.m_type = LEFT_ASSIGN; yylval.token.m_name = new string(yytext); return LEFT_ASSIGN; }
"+="			{ count(); yylval.token.m_type = ADD_ASSIGN; yylval.token.m_name = new string(yytext); return ADD_ASSIGN; }
"-="			{ count(); yylval.token.m_type = SUB_ASSIGN; yylval.token.m_name = new string(yytext); return SUB_ASSIGN; }
"*="			{ count(); yylval.token.m_type = MUL_ASSIGN; yylval.token.m_name = new string(yytext); return MUL_ASSIGN; }
"/="			{ count(); yylval.token.m_type = DIV_ASSIGN; yylval.token.m_name = new string(yytext); return DIV_ASSIGN; }
"%="			{ count(); yylval.token.m_type = MOD_ASSIGN; yylval.token.m_name = new string(yytext); return MOD_ASSIGN; }
"&="			{ count(); yylval.token.m_type = AND_ASSIGN; yylval.token.m_name = new string(yytext); return AND_ASSIGN; }
"^="			{ count(); yylval.token.m_type = XOR_ASSIGN; yylval.token.m_name = new string(yytext); return XOR_ASSIGN; }
"~="			{ count(); yylval.token.m_type = CAT_ASSIGN; yylval.token.m_name = new string(yytext); return CAT_ASSIGN; }
"|="			{ count(); yylval.token.m_type = OR_ASSIGN; yylval.token.m_name = new string(yytext); return OR_ASSIGN; }
">>"			{ count(); yylval.token.m_type = RIGHT_OP; yylval.token.m_name = new string(yytext); return RIGHT_OP; }
"<<"			{ count(); yylval.token.m_type = LEFT_OP; yylval.token.m_name = new string(yytext); return LEFT_OP; }
"++"			{ count(); yylval.token.m_type = INC_OP; yylval.token.m_name = new string(yytext); return INC_OP; }
"--"			{ count(); yylval.token.m_type = DEC_OP; yylval.token.m_name = new string(yytext); return DEC_OP; }
"->"			{ count(); yylval.token.m_type = PTR_OP; yylval.token.m_name = new string(yytext); return PTR_OP; }

";"			{ count(); yylval.token.m_type = SEMICOLON; yylval.token.m_name = new string(yytext); return SEMICOLON; }
("{"|"<%")		{ count(); yylval.token.m_type = LCURLY; yylval.token.m_name = new string(yytext); return LCURLY; }
("}"|"%>")		{ count(); yylval.token.m_type = RCURLY; yylval.token.m_name = new string(yytext); return RCURLY; }
","			{ count(); yylval.token.m_type = COMMA; yylval.token.m_name = new string(yytext); return COMMA; }
":"			{ count(); yylval.token.m_type = COLON; yylval.token.m_name = new string(yytext); return COLON; }
"="			{ count(); yylval.token.m_type = ASSIGN; yylval.token.m_name = new string(yytext); return ASSIGN; }
"("			{ count(); yylval.token.m_type = LPAREN; yylval.token.m_name = new string(yytext); return LPAREN; }
")"			{ count(); yylval.token.m_type = RPAREN; yylval.token.m_name = new string(yytext); return RPAREN; }
("["|"<:")		{ count(); yylval.token.m_type = LBRACKET; yylval.token.m_name = new string(yytext); return LBRACKET; }
("]"|":>")		{ count(); yylval.token.m_type = RBRACKET; yylval.token.m_name = new string(yytext); return RBRACKET; }
"."			{ count(); yylval.token.m_type = DOT; yylval.token.m_name = new string(yytext); return DOT; }
"&"			{ count(); yylval.token.m_type = AND; yylval.token.m_name = new string(yytext); return AND; }
"!"			{ count(); yylval.token.m_type = NOT; yylval.token.m_name = new string(yytext); return NOT; }
"~"			{ count(); yylval.token.m_type = CAT; yylval.token.m_name = new string(yytext); return CAT; }
"-"			{ count(); yylval.token.m_type = MINUS; yylval.token.m_name = new string(yytext); return MINUS; }
"+"			{ count(); yylval.token.m_type = PLUS; yylval.token.m_name = new string(yytext); return PLUS; }
"*"			{ count(); yylval.token.m_type = MUL; yylval.token.m_name = new string(yytext); return MUL; }
"/"			{ count(); yylval.token.m_type = DIV; yylval.token.m_name = new string(yytext); return DIV; }
"%"			{ count(); yylval.token.m_type = MOD; yylval.token.m_name = new string(yytext); return MOD; }
"<"			{ count(); yylval.token.m_type = OP_ST; yylval.token.m_name = new string(yytext); return OP_ST; }
">"			{ count(); yylval.token.m_type = OP_LT; yylval.token.m_name = new string(yytext); return OP_LT; }
"^"			{ count(); yylval.token.m_type = XOR; yylval.token.m_name = new string(yytext); return XOR; }
"|"			{ count(); yylval.token.m_type = OR; yylval.token.m_name = new string(yytext); return OR; }
"?"			{ count(); yylval.token.m_type = QUESTION; yylval.token.m_name = new string(yytext); return QUESTION; }

[ \t\v\n\f]		  { count(); }
{error}           { yyerror(yytext); }

%%

int yywrap()
{
    return 1;   
}

int column = 0;

void count()
{
	int i;

	for (i = 0; yytext[i] != '\0'; i++)
		if (yytext[i] == '\n')
			column = 0;
		else if (yytext[i] == '\t')
			column += 8 - (column % 8);
		else
			column++;

	ECHO;
}

int check_type()
{
/*
* pseudo code --- this is what it should check
*
*	if (yytext == type_name)
*		return(TYPE_NAME);
*
*	return(IDENTIFIER);
*/

/*
*	it actually will only return IDENTIFIER
*/

	return(IDENTIFIER);
}