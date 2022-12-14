// class headers
package com.thoughtworks.qdox.parser.impl;
import com.thoughtworks.qdox.parser.*;
%%

// class and lexer definitions
%class JFlexLexer
%public
%implements Lexer
%byaccj
%unicode
%line
%column

%{

    private int classDepth = 0;
    private int parenDepth = 0;
    private int nestingDepth = 0;
    private int annotationDepth = 0;
    private int assignmentDepth = 0;
    private int stateDepth = 0;
    private int codeblockDepth = 0;
    private int[] stateStack = new int[10];
    private int braceMode = CODEBLOCK;
    private int parenMode = -1;
    private String className;
    private boolean javaDocNewLine;
    private boolean javaDocStartedContent;
    private StringBuffer codeBody = new StringBuffer(8192);
    private boolean newMode;
    private boolean bracketMode;
    private boolean anonymousMode;
    private boolean appendingToCodeBody;
    private boolean shouldCaptureCodeBody;
    private boolean isConstructor;

    public void setCaptureCodeBody(boolean shouldCaptureCodeBody) {
        this.shouldCaptureCodeBody = shouldCaptureCodeBody;
    }

    public String text() {
        return yytext();
    }

    public int lex() throws java.io.IOException {
        return yylex();
    }
    
    public int getLine() {
        return yyline + 1;
    }

    public int getColumn() {
        return yycolumn + 1;
    }

    private void pushState(int newState) {
        stateStack[stateDepth++] = zzLexicalState;
        yybegin(newState);
    }

    private void popState() {
        yybegin(stateStack[--stateDepth]);
    }
    
    private int peekState(int relative) {
      if(relative > stateDepth) {
        return -1;
      }
      else {
        return stateStack[stateDepth - relative];
      }
    }
    
    public String getCodeBody(){
        String s = codeBody.toString();
        codeBody = new StringBuffer(8192);
        return s;
    }

%}

Eol                     = \r|\n|\r\n
WhiteSpace              = {Eol} | [ \t\f]
CommentChar             = ( [^ \t\r\n*] | "*"+ [^ \t\r\n/*] )
IntegerLiteral			= (( [1-9] ([0-9])* ) | ( "0" [xX] ([0-9]|[a-f]|[A-F])+ ) | ( "0" ([0-7])* ))
LongLiteral				= (( [1-9] ([0-9])* ) | ( "0" [xX] ([0-9]|[a-f]|[A-F])+ ) | ( "0" ([0-7])* )) [lL]
Exponent				= [eE] [+-]? ([0-9])+
FloatLiteral			= ( [0-9]+ ("." [0-9]+)? ({Exponent})? ([fF])? ) |
						  ( "." [0-9]+ ({Exponent})? ([fF])? ) |
						  ( ([0-9])+ {Exponent} ([fF])? ) |
						  ( ([0-9])+ ({Exponent})? [fF] )
DoubleLiteral			= ( [0-9]+ ("." [0-9]+)? ({Exponent})? [dD] ) |
						  ( "." [0-9]+ ({Exponent})? [dD] ) |
						  ( ([0-9])+ {Exponent} [dD] ) |
						  ( ([0-9])+ ({Exponent})? [dD] )
UnicodeChar = \\u[a-fA-F0-9]{4}						  
Id						= ([:jletter:]|{UnicodeChar}) ([:jletterdigit:]|{UnicodeChar})*
Annotation = "@" {WhiteSpace}* {Id} ("."{Id})* {WhiteSpace}*
JavadocEnd  = "*"+ "/"

%state JAVADOC JAVADOCTAG JAVADOCLINE CODEBLOCK PARENBLOCK ASSIGNMENT STRING CHAR SINGLELINECOMMENT MULTILINECOMMENT ANNOTATION ANNOSTRING ANNOCHAR ENUM

%%

<YYINITIAL, ENUM> {
    "."                 { return Parser.DOT; }
    "..."               { return Parser.DOTDOTDOT; }
    ","                 { return Parser.COMMA; }
    "*"                 { return Parser.STAR; }

    "package"           { return Parser.PACKAGE; }
    "import"            { return Parser.IMPORT; }
    "public"            { return Parser.PUBLIC; }
    "protected"         { return Parser.PROTECTED; }
    "private"           { return Parser.PRIVATE; }
    "static"            { return Parser.STATIC; }
    "final"             { return Parser.FINAL; }
    "abstract"          { return Parser.ABSTRACT; }
    "native"            { return Parser.NATIVE; }
    "strictfp"          { return Parser.STRICTFP; }
    "synchronized"      { return Parser.SYNCHRONIZED; }
    "transient"         { return Parser.TRANSIENT; }
    "volatile"          { return Parser.VOLATILE; }
    "throws"            { return Parser.THROWS; }
    "extends"           { return Parser.EXTENDS; }
    "implements"        { return Parser.IMPLEMENTS; }
    "super"             { return Parser.SUPER; }

    "["                 { nestingDepth++; return Parser.SQUAREOPEN; }
    "]"                 { nestingDepth--; return Parser.SQUARECLOSE; }
    ")"                 { nestingDepth--; return Parser.PARENCLOSE; }
    "<"                 { return Parser.LESSTHAN; }
    ">"                 { return Parser.GREATERTHAN; }
    "&"                 { return Parser.AMPERSAND; }
    "?"                 { return Parser.QUERY; }

    "@" {WhiteSpace}* "interface" {
      	classDepth++;
        braceMode = YYINITIAL;
        return Parser.ANNOINTERFACE;
	  }

    "class"             {
        classDepth++;
        braceMode = YYINITIAL;
        return Parser.CLASS; 
    }
    
    "interface"         { 
        classDepth++;
        braceMode = YYINITIAL;
        return Parser.INTERFACE;
    }
    
    "enum"              {
        classDepth++;
        braceMode = ENUM;
        return Parser.ENUM;
    }
    {Annotation} "(" {
        parenMode = ANNOTATION;
        yypushback(text().length()-1);
        getCodeBody(); /* reset codebody */
        return Parser.AT;
    }
    "@"                 {
        return Parser.AT;
    }
    "{"                 {
        if(braceMode >= 0) {
          if(braceMode == ENUM) {
            isConstructor = true;
          } else if (braceMode == CODEBLOCK) {
              getCodeBody(); /* reset codebody */
              appendingToCodeBody = true;
          }
          pushState(braceMode);
          braceMode = -1;
          yypushback(1); /* (re)enter brace in right mode */
        }
        else {
          nestingDepth++;
          braceMode = CODEBLOCK;
          return Parser.BRACEOPEN;
        }
    }
    "}"  { 
        nestingDepth--;
        classDepth--;
        popState();
        braceMode = CODEBLOCK;
        return Parser.BRACECLOSE; 
    }

    "/**" ~"*/" {
        pushState(JAVADOC);
        yypushStream(new java.io.StringReader(text().substring(2))); 
        return Parser.JAVADOCSTART;
    }

    "=" {WhiteSpace}* { 
        assignmentDepth = nestingDepth;
        getCodeBody(); /* reset codebody */
        appendingToCodeBody = true;
        pushState(ASSIGNMENT);
    }
    "default"           { 
        assignmentDepth = nestingDepth;
        appendingToCodeBody = true;
        pushState(ASSIGNMENT);
    }
    {Id} {
        return Parser.IDENTIFIER;
    }
}
<YYINITIAL> {
    ";"  { return Parser.SEMI; }
    "("  {
            nestingDepth++;
            if( parenMode >= 0 ) {
              annotationDepth = nestingDepth;
              pushState(parenMode);
              parenMode = -1;
            }
            return Parser.PARENOPEN;
          }
}
<ENUM> {
    ";"  { isConstructor = false; return Parser.SEMI; }
    "("  {
            nestingDepth++;
            if(parenMode >= 0) {
              annotationDepth = nestingDepth;
              pushState(parenMode);
              parenMode = -1;
              return Parser.PARENOPEN;
            }
            else {
              if(isConstructor) {
                parenDepth = classDepth;
                pushState(PARENBLOCK);
                return Parser.PARENBLOCK;
              }
              else {
                return Parser.PARENOPEN;
              }
            }
          }
}
<JAVADOC> {
    "@"               { yypushback(1); pushState(JAVADOCTAG); }
    [^ \t\r*@]		  { yypushback(1); pushState(JAVADOCLINE); }
    "*"+ [ \t]* / "@" { pushState(JAVADOCTAG); }
    "*"+ [ \t]?       { pushState(JAVADOCLINE); }
    {JavadocEnd}      { popState(); yypopStream(); return Parser.JAVADOCEND; }
}
<JAVADOCLINE> {
  ~{Eol}                           { popState(); return Parser.JAVADOCLINE; }
  .* [^ \t*] / [ \t]* {JavadocEnd} { popState(); return Parser.JAVADOCLINE;}
  {JavadocEnd}                     { popState(); popState(); yypopStream(); return Parser.JAVADOCEND; }
}

<JAVADOCTAG> {
  "@" [^ \t\n\r]+ / {JavadocEnd} { popState(); return Parser.JAVADOCTAG;  }
  "@" [^ \t\n\r]+                { return Parser.JAVADOCTAG; }
  [ \t]+                         { popState();pushState(JAVADOCLINE);}
  {Eol}                          { popState();return Parser.JAVADOCLINE;}
}

<CODEBLOCK> {
     "{"  { 
            if(codeblockDepth++ > 0 ) {
            codeBody.append('{');
            }  
          }
     "}"                 {
        if (--codeblockDepth == 0) {
            popState();
            appendingToCodeBody = false;
            braceMode = CODEBLOCK;
            return Parser.CODEBLOCK;
        } else {
            codeBody.append('}');
        }
    }
}

<ANNOTATION> {
	"("                 { ++ nestingDepth; return Parser.PARENOPEN; }
    ")"                 { if( nestingDepth-- == annotationDepth) { popState(); } return Parser.PARENCLOSE; }

	","                 { return Parser.COMMA; }
    "="                 { return Parser.EQUALS; }

	"{"                 { nestingDepth++; return Parser.BRACEOPEN; }
    "}"                 { nestingDepth--; return Parser.BRACECLOSE; }

	"\""                { appendingToCodeBody=true; codeBody.append("\""); pushState(ANNOSTRING); }
    "\'"                { appendingToCodeBody=true; codeBody.append("\'"); pushState(ANNOCHAR); }

	"."                 { return Parser.DOT; }

    "?"                 { return Parser.QUERY; }
    ":"                 { return Parser.COLON; }
    "<<"                { return Parser.LESSTHAN2; }
    ">>>"               { return Parser.GREATERTHAN3; }
    ">>"                { return Parser.GREATERTHAN2; }
    "=="                { return Parser.EQUALS2; }
    "!="                { return Parser.NOTEQUALS; }
    "<"                 { return Parser.LESSTHAN; }
    ">"                 { return Parser.GREATERTHAN; }
    "<="                { return Parser.LESSEQUALS; }
    ">="                { return Parser.GREATEREQUALS; }
    "*"                 { return Parser.STAR; }
    "/"                 { return Parser.SLASH; }
    "%"                 { return Parser.PERCENT; }
    "+"                 { return Parser.PLUS; }
    "-"                 { return Parser.MINUS; }
    
    "byte"              { return Parser.BYTE; }
    "char"              { return Parser.CHAR; }
    "short"             { return Parser.SHORT; }
    "int"               { return Parser.INT; }
    "long"              { return Parser.LONG; }
    "float"             { return Parser.FLOAT; }
    "double"            { return Parser.DOUBLE; }

    "&&"                { return Parser.AMPERSAND2; }
    "||"                { return Parser.VERTLINE2; }
    "!"                	{ return Parser.EXCLAMATION; }

    "&"                 { return Parser.AMPERSAND; }
    "|"                 { return Parser.VERTLINE; }
    "^"                	{ return Parser.CIRCUMFLEX; }
    "~"                 { return Parser.TILDE; }

	{IntegerLiteral}	{ return Parser.INTEGER_LITERAL; }
	{LongLiteral}		{ return Parser.LONG_LITERAL; }
	{FloatLiteral}		{ return Parser.FLOAT_LITERAL; }
	{DoubleLiteral}		{ return Parser.DOUBLE_LITERAL; }
	"true" | "false"	{ return Parser.BOOLEAN_LITERAL; }
	
	"class"				{ return Parser.CLASS; }

	{Id} {
        return Parser.IDENTIFIER;
    }

	"@" 				{ return Parser.AT; }
}

<ANNOSTRING> {
	"\""            { codeBody.append("\""); popState(); appendingToCodeBody=false; return Parser.STRING_LITERAL; }
	"\\\""          { codeBody.append("\\\""); }
	"\\\\"          { codeBody.append("\\\\"); }
}

<ANNOCHAR> {
	\'              { codeBody.append("\'"); popState(); appendingToCodeBody=false; return Parser.CHAR_LITERAL; }
	"\\'"           { codeBody.append("\\'"); }
	"\\\\"          { codeBody.append("\\\\"); }
}

<PARENBLOCK> {
    "("             { 
        nestingDepth++; 
        if (appendingToCodeBody) { codeBody.append("("); }
    }
    ")"             {
		nestingDepth--;
		if (appendingToCodeBody) { codeBody.append(")"); }
        if (nestingDepth == parenDepth) {
            popState();
        }
    }
}

<ASSIGNMENT> {
    ";"                 { 
        if (nestingDepth == assignmentDepth) {
            appendingToCodeBody = true;
            newMode = false;
            popState(); 
            return Parser.SEMI; 
        } else {
            codeBody.append(';');
        }
    }
    ","                 {
        if (nestingDepth == assignmentDepth) {
            appendingToCodeBody = true;
            popState(); 
            return Parser.COMMA; 
        } else {
            codeBody.append(',');
        }
    }
    "{"                 {
        codeBody.append('{');
		anonymousMode = true;
		nestingDepth++;
    }
    "}"                 {
		codeBody.append('}');
        if (anonymousMode) {
            nestingDepth--;
        	if (nestingDepth==assignmentDepth) { 
                anonymousMode=false;
            }
        }
    }

    "("                 { 
        codeBody.append('('); 
        parenDepth = nestingDepth++; 
        pushState(PARENBLOCK); 
    }
    ")"                 {
        codeBody.append(')');
        nestingDepth--; 
        if (nestingDepth < assignmentDepth) {
            appendingToCodeBody = true; 
            popState(); 
            return Parser.PARENCLOSE; 
        }
    }
    "["                 { codeBody.append('['); bracketMode = true; nestingDepth++; }
    "]"                 { codeBody.append(']'); bracketMode = false; nestingDepth--; }
    "new"               {
        codeBody.append("new");
        if (nestingDepth==assignmentDepth) {
            newMode=true;
        } 
    }
    "." [ \t\r\n]* / "<" {
      codeBody.append('.');
      newMode = true;
    }
    "<"                 {
        codeBody.append('<');
        if (!bracketMode && newMode && !anonymousMode) {
            nestingDepth++; 
        }
    }
    ">"                 {
        codeBody.append('>');
        if (!anonymousMode) {
	        if (!bracketMode && newMode) {
    	        nestingDepth--;
    	    	if (nestingDepth==assignmentDepth) { 
    	            newMode=false;
    	        }
        	}
        }
    }
}

<ASSIGNMENT, YYINITIAL, CODEBLOCK, PARENBLOCK, ENUM> {
    "\""                { if (appendingToCodeBody) { codeBody.append('"');  } pushState(STRING); }
    \'                  { if (appendingToCodeBody) { codeBody.append('\''); } pushState(CHAR); }
}

<ASSIGNMENT, YYINITIAL, CODEBLOCK, PARENBLOCK, ENUM, ANNOTATION> {
  "//"                { if (appendingToCodeBody) { codeBody.append("//"); } pushState(SINGLELINECOMMENT); }
  "/*"                { if (appendingToCodeBody) { codeBody.append("/*"); } pushState(MULTILINECOMMENT); }
  "/**/"              { if (appendingToCodeBody) { codeBody.append("/**/"); } }
}

<CODEBLOCK, ASSIGNMENT> { 
    .|{WhiteSpace}	    { codeBody.append(yytext()); }
}

<STRING> {
    "\""                { if (appendingToCodeBody) { codeBody.append('"');    } popState(); }
    "\\\""              { if (appendingToCodeBody) { codeBody.append("\\\""); } }
    "\\\\"              { if (appendingToCodeBody) { codeBody.append("\\\\"); } }
}

<CHAR> {
    \'                  { if (appendingToCodeBody) { codeBody.append('\'');    } popState(); }
    "\\'"               { if (appendingToCodeBody) { codeBody.append("\\'");  } }
    "\\\\"              { if (appendingToCodeBody) { codeBody.append("\\\\"); } }
}

<SINGLELINECOMMENT> {
    {Eol}               { if (appendingToCodeBody) { codeBody.append(yytext()); } popState(); }
}

<MULTILINECOMMENT> {
    "*/"                { if (appendingToCodeBody) { codeBody.append("*/"); } popState(); }
}

.|\r|\n|\r\n            { if (appendingToCodeBody) { codeBody.append(yytext()); } }
