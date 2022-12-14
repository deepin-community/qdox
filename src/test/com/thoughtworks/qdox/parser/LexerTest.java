package com.thoughtworks.qdox.parser;

import junit.framework.TestCase;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.qdox.parser.impl.JFlexLexer;
import com.thoughtworks.qdox.parser.impl.Parser;

public class LexerTest extends TestCase {

    private static Map tokens;

    static {
        tokens = new HashMap();
        Field[] tokenFlds = Parser.class.getDeclaredFields();
        for (int i = 0; i < tokenFlds.length; i++) {
            Field f = tokenFlds[i];
            try {
                if (!f.getName().startsWith("YY")) {
                    tokens.put(new Integer(f.getShort(Parser.class)), f.getName());
                }
            } catch (Exception e) {
                // we don't care!
            }
        }
    }

    public LexerTest(String s) {
        super(s);
    }

    public void testEmptyInput() throws Exception {
        String in = "";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(0, lexer);
    }

    public void testNewlines() throws Exception {
        String in = "DOS\r\nUNIX\nMAC\r";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "DOS", lexer);
        assertLex(Parser.IDENTIFIER, "UNIX", lexer);
        assertLex(Parser.IDENTIFIER, "MAC", lexer);
        assertLex(0, lexer);
    }

    public void testStaticBlock() throws Exception {
        String in = ""
                + "class X { "
                + " static { "
                + "   something(); "
                + " } "
                + "} ";

        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.STATIC, lexer);
        assertLex(Parser.CODEBLOCK, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testFieldAssignment() throws Exception {
        checkAssignment("x");
        checkAssignment("(map.isEmpty ? 1 : -1)");
        checkAssignment("0");
        checkAssignment("\"hello\"");
        checkAssignment("new Thingy()");
        checkAssignment("new Thingy(\"xxx\", 3, x.getBlah())");
        checkAssignment("new Thingy(\"xx;x\", 3, x.getBlah())");
        checkAssignment("StaticClass.intance()");
        checkAssignment("[1,2,3]");
        checkAssignment("/* , ; } */");
    }

    public void testAnonymousInnerClassAssignment() throws Exception {
        checkAssignment("new Thingifier() { void doThings(int x) { blah(); } }");
        checkAssignment("new Thingifier() { void doThings(int x) { a = \"aaa\"; } }");
    }

    public void testGenericTypeAssignment() throws Exception {
        // QDOX-77
        checkAssignment("new HashMap<String,Integer>");
    }

    public void testFieldsContainingLessThanOrGreaterThanInAssignment() throws Exception {
        // QDOX-71 - this is really important as it is common to see in all versions of the JDK.
        // Please don't disable this test -joe.
        checkAssignment("x < y");
        checkAssignment("x > y");
        checkAssignment("x << y");
        checkAssignment("x >> y");
        checkAssignment("x<y>z");
    }
    
    private void checkAssignment(String assignment) throws IOException {
        String in = ""
                + "class X { "
                + " int x = " + assignment + "; "
                + "} ";

        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);

        assertLex(Parser.IDENTIFIER, "int", lexer);
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);

        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testMultipleFields() throws Exception {
        String in = ""
        + "class X { "
        + " int x, y = 2; "
        + "} ";

        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);

        assertLex(Parser.IDENTIFIER, "int", lexer);
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.COMMA, lexer);
        assertLex(Parser.IDENTIFIER, "y", lexer);
        assertLex(Parser.SEMI, lexer);
        
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testSpecialCharsInIdentifier() throws Exception {
        String in = "a_b x$y z80";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "a_b", lexer);
        assertLex(Parser.IDENTIFIER, "x$y", lexer);
        assertLex(Parser.IDENTIFIER, "z80", lexer);
        assertLex(0, lexer);
    }

    public void testUnicodeInTest() throws Exception {
        checkAssignment("\"\u0000\""); 
    }

    public void testUnicodeInFile() throws Exception {
        Lexer lexer = new JFlexLexer(new FileReader("src/test/com/thoughtworks/qdox/testdata/Unicode.java"));
        assertLex(Parser.PACKAGE, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.DOT, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.DOT, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.DOT, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);

        assertLex(Parser.IDENTIFIER, "String", lexer);
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);

        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testUnicodeIdentifiers() throws Exception {
        // \u0391 == uppercase Greek "Alpha"
        assertSingleLex("\u0391", Parser.IDENTIFIER);
        // \u00f6 == lowercase o + diaeresis
        assertSingleLex("f\u00f6rnamn", Parser.IDENTIFIER); 
    }

    public void testInnerClass() throws Exception {
        String in = ""
                + "class X { "
                + " class InnerClass { "
                + "   int x = 1; "
                + " } "
                + " int y = 2; "
                + "} ";

        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, "InnerClass", lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.IDENTIFIER, "int", lexer);
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(Parser.IDENTIFIER, "int", lexer);
        assertLex(Parser.IDENTIFIER, "y", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testCurliesInStringsOrChars() throws Exception {

        checkAssignment("\"{\"");
        checkAssignment("\"}\"");
        checkAssignment("'}'");
        checkAssignment("'{'");

    }

    public void testDoubleBackSlashesInStringsOrChars() throws Exception {

        checkAssignment("\"\\\\\""); // x = "\\"
        checkAssignment("'\\\\'"); // x = '\\'

    }

    public void testFunnyCharsInStringsOrChars() throws Exception {
        checkAssignment("\"???????????\"");
        checkAssignment("'???????????'");
    }

    public void testQuoteInCharInCodeBlock() throws Exception {
        String in = "{'\"'}";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CODEBLOCK, lexer);
        assertLex(0, lexer);
    }

    public void testDoubleQuotesInCharInAssignment() throws Exception {
        String in = "x = '\"';";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(0, lexer);
    }

    public void testSingleQuoteInCharInAssignment() throws Exception {
        String in = "x = '\\'';";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(0, lexer);
    }

    public void testStringWithDoubleQuotesIn() throws Exception {
        String in = "x = \"blah \\\" blah\";";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(0, lexer);
    }

    public void testCommentsWithSingleQuoteInCodeBlock() throws Exception {
        String in = "{ /* ' */ }";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CODEBLOCK, lexer);
        assertLex(0, lexer);
    }

    public void testCommentsWithDoubleQuotesInCodeBlock() throws Exception {
        String in = "{ /* \" */ }";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CODEBLOCK, lexer);
        assertLex(0, lexer);
    }

    public void testCommentsThatEndImmediately() throws Exception {
        String in = "/**/ class";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.CLASS, lexer);
        assertLex(0, lexer);
    }

    public void testCommentsWithQuotesInAssignment() throws Exception {
        String in = "a x = y /* don't do stuff*/;";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "a", lexer);
        assertLex(Parser.IDENTIFIER, "x", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(0, lexer);

        in = "a z = \n"
                + "// thing's thing \n"
                + "0;";
        lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "a", lexer);
        assertLex(Parser.IDENTIFIER, "z", lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(0, lexer);
    }

    public void testDocletTags() throws Exception {
        String in = ""
                + "/**\n"
                + " * @hello world\n"
                + " * @a b c d\n"
                + " * @bye\n"
                + " * @bye:bye\n"
                + " */";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);

        assertLex(Parser.JAVADOCLINE, "\n",lexer);
        assertLex(Parser.JAVADOCTAG, "@hello", lexer);
        assertLex(Parser.JAVADOCLINE, "world\n", lexer);

        assertLex(Parser.JAVADOCTAG, "@a", lexer);
        assertLex(Parser.JAVADOCLINE, "b c d\n", lexer);

        assertLex(Parser.JAVADOCTAG, "@bye", lexer);
        assertLex(Parser.JAVADOCLINE, "\n",lexer);

        assertLex(Parser.JAVADOCTAG, "@bye:bye", lexer);
        assertLex(Parser.JAVADOCLINE, "\n",lexer);

        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }

    public void testOneLinerDocComment() throws Exception {
        String in = "/** @hello world */";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);

        assertLex(Parser.JAVADOCTAG, "@hello", lexer);
        assertLex(Parser.JAVADOCLINE, "world", lexer);

        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }

    public void testCompressedDocComment() throws Exception {
        String in = "/**@foo bar*/";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);
        
        assertLex(Parser.JAVADOCTAG, "@foo", lexer);
        assertLex(Parser.JAVADOCLINE, "bar", lexer);
        
        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }

    public void testDeepJavadocTag() throws Exception {
        String in = "  /** *  *** * @m x \n" +
        		"*/";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);
        
        assertLex(Parser.JAVADOCLINE, "*  *** * @m x \n", lexer);
        
        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer); 
    }

    public void testDocCommentContainingAtSymbols() throws Exception {
        String in = ""
            + "/**\n"
            + " * joe@truemesh.com\n"
            + " * {@link here}.\n"
            + " * me @home\n"
            + " * geeks @ play\n"
            + " */";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);

        assertLex(Parser.JAVADOCLINE, "\n", lexer);
        assertLex(Parser.JAVADOCLINE, "joe@truemesh.com\n", lexer);
        assertLex(Parser.JAVADOCLINE, "{@link here}.\n", lexer);
        assertLex(Parser.JAVADOCLINE, "me @home\n", lexer);
        assertLex(Parser.JAVADOCLINE, "geeks @ play\n", lexer);

        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }

    public void testDocCommentContainingStars() throws Exception {
        String in = ""
                + "/**\n"
                + " * 5 * 4\n"
                + " * SELECT COUNT(*)\n"
                + " * **stars**everywhere** \n"
                + " */";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);

        assertLex(Parser.JAVADOCLINE, "\n", lexer);
        assertLex(Parser.JAVADOCLINE, "5 * 4\n", lexer);
        assertLex(Parser.JAVADOCLINE, "SELECT COUNT(*)\n", lexer);
        assertLex(Parser.JAVADOCLINE, "**stars**everywhere** \n", lexer);

        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }

    public void testExtraStarsAreIgnoredAtStartAndEnd() throws Exception {
        String in = ""
                + "/*****\n"
                + " * blah\n"
                + " *****/";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);
        assertLex(Parser.JAVADOCLINE, "\n", lexer);
        assertLex(Parser.JAVADOCLINE, "blah\n", lexer);
        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }

    public void testExtraStarsCompressed() throws Exception {
        String in = ""
                + "/***blah***/public";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);
        assertLex(Parser.JAVADOCLINE, "blah", lexer);
        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(Parser.PUBLIC, lexer);
        assertLex(0, lexer);
    }

    public void testIgnoreStarPrefix() throws Exception {
        String in = ""
            + "/**\n"
            + " * simple\n"
            + "\t    * indented\n"
            + " *nospace\n"
            + " *** multistar\n"
            + " *\n"
            + " */";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);
        
        assertLex(Parser.JAVADOCLINE, "\n", lexer);
        assertLex(Parser.JAVADOCLINE, "simple\n", lexer);
        assertLex(Parser.JAVADOCLINE, "indented\n", lexer);
        assertLex(Parser.JAVADOCLINE, "nospace\n", lexer);
        assertLex(Parser.JAVADOCLINE, "multistar\n", lexer);
        assertLex(Parser.JAVADOCLINE, "\n", lexer);
        
        assertLex(Parser.JAVADOCEND, lexer);
        assertLex(0, lexer);
    }
    
    // QDOX-200
    public void testCompactJavaDocTag() throws Exception {
        String in = "/** @foo*/";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.JAVADOCSTART, lexer);
        assertLex(Parser.JAVADOCTAG, "@foo", lexer);
        assertLex(Parser.JAVADOCEND, lexer);      
        assertLex(0, lexer);
    }

    public void testArrayTokens() throws Exception {
        String in = "String[] []o[]";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "String", lexer);
        assertLex(Parser.SQUAREOPEN, lexer);
        assertLex(Parser.SQUARECLOSE, lexer);
        assertLex(Parser.SQUAREOPEN, lexer);
        assertLex(Parser.SQUARECLOSE, lexer);
        assertLex(Parser.IDENTIFIER, "o", lexer);
        assertLex(Parser.SQUAREOPEN, lexer);
        assertLex(Parser.SQUARECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testKeyWords() throws Exception {
        assertSingleLex("package", Parser.PACKAGE);
        assertSingleLex("import", Parser.IMPORT);
        assertSingleLex("public", Parser.PUBLIC);
        assertSingleLex("protected", Parser.PROTECTED);
        assertSingleLex("private", Parser.PRIVATE);
        assertSingleLex("static", Parser.STATIC);
        assertSingleLex("final", Parser.FINAL);
        assertSingleLex("abstract", Parser.ABSTRACT);
        assertSingleLex("native", Parser.NATIVE);
        assertSingleLex("strictfp", Parser.STRICTFP);
        assertSingleLex("synchronized", Parser.SYNCHRONIZED);
        assertSingleLex("transient", Parser.TRANSIENT);
        assertSingleLex("volatile", Parser.VOLATILE);
        assertSingleLex("class", Parser.CLASS);
        assertSingleLex("interface", Parser.INTERFACE);
        assertSingleLex("throws", Parser.THROWS);
        assertSingleLex("extends", Parser.EXTENDS);
        assertSingleLex("implements", Parser.IMPLEMENTS);
        assertSingleLex("super", Parser.SUPER);
    }

    public void testTypeTokens() throws Exception {
        String in = "Map<? extends A & B, List<String>>";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.IDENTIFIER, "Map", lexer);
        assertLex(Parser.LESSTHAN, lexer);
        assertLex(Parser.QUERY, lexer);
        assertLex(Parser.EXTENDS, lexer);
        assertLex(Parser.IDENTIFIER, "A", lexer);
        assertLex(Parser.AMPERSAND, lexer);
        assertLex(Parser.IDENTIFIER, "B", lexer);
        assertLex(Parser.COMMA, lexer);
        assertLex(Parser.IDENTIFIER, "List", lexer);
        assertLex(Parser.LESSTHAN, lexer);
        assertLex(Parser.IDENTIFIER, "String", lexer);
        assertLex(Parser.GREATERTHAN, lexer);
        assertLex(Parser.GREATERTHAN, lexer);
        assertLex(0, lexer);
    }

    public void testAnnotationDeclarationTokens() throws Exception {
        String in = "" 
            + "public @interface Copyright {\n" 
            + "    int year();\n" 
            + "    String assignee() default \"The CodeHaus\";\n"
            + "}\n";
        Lexer lexer = new JFlexLexer(new StringReader(in));

        assertLex(Parser.PUBLIC, lexer);
        assertLex(Parser.ANNOINTERFACE, lexer);
        assertLex(Parser.IDENTIFIER, "Copyright", lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.IDENTIFIER, "int", lexer);
        assertLex(Parser.IDENTIFIER, "year", lexer);
        assertLex(Parser.PARENOPEN, lexer);
        assertLex(Parser.PARENCLOSE, lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.IDENTIFIER, "String", lexer);
        assertLex(Parser.IDENTIFIER, "assignee", lexer);
        assertLex(Parser.PARENOPEN, lexer);
        assertLex(Parser.PARENCLOSE, lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testAnnotationTokens() throws Exception {
        String in = ""
            + "@Copyright (year = 2004, month = \"Jan\")\n"
            + "@Note(\"Just ignore me\")\n" 
            + "public class LexerTest extends TestCase {}\n";
        Lexer lexer = new JFlexLexer(new StringReader(in));

        assertLex(Parser.AT, "@", lexer);
        assertLex(Parser.IDENTIFIER, "Copyright", lexer);
        assertLex(Parser.PARENOPEN, lexer);
        assertLex(Parser.IDENTIFIER, "year", lexer);
        assertLex(Parser.EQUALS, lexer);
        assertLex(Parser.INTEGER_LITERAL, "2004", lexer);
        assertLex(Parser.COMMA, lexer);
        assertLex(Parser.IDENTIFIER, "month", lexer);
        assertLex(Parser.EQUALS, lexer);
        assertLex(Parser.STRING_LITERAL, "\"", lexer);
        // TODO the above should be ...
        // assertLex(Parser.STRING_LITERAL, "Jan", lexer);
        assertLex(Parser.PARENCLOSE, lexer);
        assertLex(Parser.AT, "@", lexer);
        assertLex(Parser.IDENTIFIER, "Note", lexer);
        assertLex(Parser.PARENOPEN, lexer);
        assertLex(Parser.STRING_LITERAL, lexer);
        assertLex(Parser.PARENCLOSE, lexer);
        assertLex(Parser.PUBLIC, lexer);
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, "LexerTest", lexer);
        assertLex(Parser.EXTENDS, lexer);
        assertLex(Parser.IDENTIFIER, "TestCase", lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testQDOX134_MoreAnnotationTokens() throws Exception {
        String in = ""
            + "@myTag name=TestClass attrs=Something1,Something2,Something3\n"
            + "public class LexerTest extends TestCase {}\n";
        Lexer lexer = new JFlexLexer(new StringReader(in));

        assertLex(Parser.AT, "@", lexer);
        assertLex(Parser.IDENTIFIER, "myTag", lexer);
        assertLex(Parser.IDENTIFIER, "name", lexer);
        assertLex(Parser.COMMA, lexer);
        assertLex(Parser.IDENTIFIER, "Something2", lexer);
        assertLex(Parser.COMMA, lexer);
        assertLex(Parser.IDENTIFIER, "Something3", lexer);
        assertLex(Parser.PUBLIC, lexer);
        assertLex(Parser.CLASS, lexer);
        assertLex(Parser.IDENTIFIER, "LexerTest", lexer);
        assertLex(Parser.EXTENDS, lexer);
        assertLex(Parser.IDENTIFIER, "TestCase", lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }

    public void testEnumConstructor() throws Exception {
        String in = "enum Foo { a(\"hello\"); int someField; }";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.ENUM, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.PARENBLOCK, lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
    }

    public void testEnumWithMethods() throws Exception {
        String in = ""
                + "enum Animal {"
                + " DUCK { public void speak() { System.out.println(\"quack!\"); } }"
                + "}";
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.ENUM, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.CODEBLOCK, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
    }

    private void assertSingleLex(String in, short expectedLex) throws Exception {
        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(expectedLex, lexer);
        assertLex(0, lexer);
    }


    private void assertLex(int expectedToken, Lexer lexer) throws IOException {
        Object expected = tokens.get(new Integer(expectedToken));
        Object actual = tokens.get(new Integer(lexer.lex()));
        assertEquals(expected, actual);
    }

    private void assertLex(int expectedToken, String expectedText, Lexer lexer) throws IOException {
        assertLex(expectedToken, lexer);
        assertEquals(expectedText, lexer.text());
    }

    // for QDOX-140
    public void testNonAsciiMethodNameDoesNotCrashLexerButChewsUpUnicodeEscapedSequencesBadly() throws Exception {
        String in = ""
                + "interface X { "
                + "   void paramWithNonAsciis\\u00E4\\u00F6\\u00FC\\u00DF();"
                + "} ";

        Lexer lexer = new JFlexLexer(new StringReader(in));
        assertLex(Parser.INTERFACE, lexer);
        assertLex(Parser.IDENTIFIER, lexer);
        assertLex(Parser.BRACEOPEN, lexer);
        assertLex(Parser.IDENTIFIER, "void", lexer);
        assertLex(Parser.IDENTIFIER, "paramWithNonAsciis\\u00E4\\u00F6\\u00FC\\u00DF", lexer);
        assertLex(Parser.PARENOPEN, lexer);
        assertLex(Parser.PARENCLOSE, lexer);
        assertLex(Parser.SEMI, lexer);
        assertLex(Parser.BRACECLOSE, lexer);
        assertLex(0, lexer);
    }
    
    //for QDOX-158
    public void testAnnotationWithMultipleParameters() throws Exception {
    	String in = 
    			"@MyFunction.MyInterface( prefix1 = \"abc\", prefix2 = \"abc\" )";
    	Lexer lexer = new JFlexLexer(new StringReader(in));
    	assertLex(Parser.AT, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.DOT, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PARENOPEN, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.EQUALS, lexer);
    	assertLex(Parser.STRING_LITERAL, lexer);
    	assertLex(Parser.COMMA, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.EQUALS, lexer);
    	assertLex(Parser.STRING_LITERAL, lexer);
    	assertLex(Parser.PARENCLOSE, lexer);
    }

    public void testSimpleAnnotation() throws Exception {
    	String in = "@Override\n public boolean isReadOnly(final ELContext context)";
    	Lexer lexer = new JFlexLexer(new StringReader(in));
    	assertLex(Parser.AT, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PUBLIC, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PARENOPEN, lexer);
    	assertLex(Parser.FINAL, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PARENCLOSE, lexer);
    }
    
    public void testMultipleRowAnnotation() throws Exception {
    	String in = "@JSFComponent\n  (name = \"h:inputHidden\")";
    	Lexer lexer = new JFlexLexer(new StringReader(in));
    	assertLex(Parser.AT, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PARENOPEN, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.EQUALS, lexer);
    	assertLex(Parser.STRING_LITERAL, lexer);
    	assertLex(Parser.PARENCLOSE, lexer);
    }
    
    public void testEnumWithAnnotations() throws Exception {
    	String in = "class Foo {\n" +
		"public enum BasicType {\n" +
		"@XmlEnumValue(\"text\")\n" +
		"VALUE(\"value\"); }\n" +
		"}";  
    	Lexer lexer = new JFlexLexer(new StringReader(in));
    	assertLex(Parser.CLASS, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.BRACEOPEN, lexer);
    	assertLex(Parser.PUBLIC, lexer);
    	assertLex(Parser.ENUM, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.BRACEOPEN, lexer);
    	assertLex(Parser.AT, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PARENOPEN, lexer);
    	assertLex(Parser.STRING_LITERAL, lexer);
    	assertLex(Parser.PARENCLOSE, lexer);
    	assertLex(Parser.IDENTIFIER, lexer);
    	assertLex(Parser.PARENBLOCK, lexer);
    	assertLex(Parser.SEMI, lexer);
    }
}
