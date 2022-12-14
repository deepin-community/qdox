package com.thoughtworks.qdox;

import junit.framework.TestCase;

import java.io.StringReader;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.parser.Lexer;
import com.thoughtworks.qdox.parser.impl.JFlexLexer;

public class EnumsTest extends TestCase {

    public void testAddEmptyEnumsToModel() {

        String source = ""
                + "public enum Enum1 {}\n"
                + "enum Enum2 {;}\n";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        assertTrue(javaDocBuilder.getClassByName("Enum1").isEnum());
        assertTrue(javaDocBuilder.getClassByName("Enum2").isEnum());
    }

    public void testAddSimpleEnumsToModel() {

        String source = ""
                + "public enum Enum1 { a, b }"
                + "class X { "
                + "  enum Enum2 { c, /** some doc */ d } "
                + "  int someField; "
                + "}";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        JavaClass cls = javaDocBuilder.getClassByName("X");
        assertEquals("int", cls.getFieldByName("someField").getType().getValue()); // sanity check
        assertTrue(javaDocBuilder.getClassByName("Enum1").isEnum());
        assertTrue(javaDocBuilder.getClassByName("X$Enum2").isEnum());
    }    
    
    public void testAddEnumImplementingInterfaceToModel() {
        String source = ""
                + "public enum Enum1 implements java.io.Serializable { a, b }";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        JavaClass cls = javaDocBuilder.getClassByName("Enum1");
        assertTrue(cls.isEnum());
        assertTrue(cls.isA("java.io.Serializable"));
    }

    public void testAddEnumWithAnnotationToModel() {
        String source = ""
                + "public enum Enum1 implements java.io.Serializable { a, @Deprecated b }";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        JavaClass cls = javaDocBuilder.getClassByName("Enum1");
        assertTrue(cls.isEnum());
        assertTrue(cls.isA("java.io.Serializable"));
    }

    public void testAddEnumWithFieldAndConstructorsToModel() {

        String source = ""
                + "class X {\n"
                + "    enum EnumWithConstructors {\n"
                + "        c(\"hello\"), d();\n"
                + "\n"
                + "        int someField;\n"
                + "\n"
                + "        EnumWithConstructors() {}\n"
                + "\n"
                + "        EnumWithConstructors(String x) {\n"
                + "        }\n"
                + "    }\n"
                + "}";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        JavaClass cls = javaDocBuilder.getClassByName("X$EnumWithConstructors");
        assertTrue(cls.isEnum());
        assertEquals("int", cls.getFieldByName("someField").getType().getValue()); // sanity check
    }

    public void testAddEnumsWithMethodsToModel() throws Exception {
        String source = ""
                + "public enum Animal {\n"
                + "    \n"
                + "    DUCK    { public void speak() { System.out.println(\"quack!\"); } },\n"
                + "    CHICKEN { public void speak() { System.out.println(\"cluck!\"); } };\n"
                + "\n"
                + "    public abstract void speak();\n"
                + "}";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        assertTrue(javaDocBuilder.getClassByName("Animal").isEnum());
    }

    //Verify test case from QDOX-74
    public void testAddEnumsWithConstructorsToModel() throws Exception {
        String source = ""
                + "public enum AccountType {\n"
                + "    \n"
                + "    ADMINISTRATOR (1, \"Administrator\"),\n"
                + "    CUSTOMER (2, \"Customer\"),\n"
                + "\n"
                + "}";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        assertTrue(javaDocBuilder.getClassByName("AccountType").isEnum());
    }
    
    //Verify test case from QDOX-74
    public void testAddEnumsThatDontEndInSemicolon() throws Exception {
        String source = ""
                + "public enum Foo { BAR }\n";

        JavaDocBuilder javaDocBuilder = new JavaDocBuilder();
        javaDocBuilder.addSource(new StringReader(source));

        assertTrue(javaDocBuilder.getClassByName("Foo").isEnum());
    }
    
    
    public void testEnumBeforeClass() throws Exception {
        String source = "" +
        	"package org.carrot2.util.attribute.constraint;" +
        	"public class Test" +
        	"{" +
        	"public enum TestValueSet" +
        	"{ VALUE_1 }" +
        	"static class AnnotationContainer" +
        	"{ @ValueHintEnum(values = TestValueSet.class) String hint; }" +
        	"}";
        new JavaDocBuilder().addSource(new StringReader(source));
   }
    
    public void testEnumAfterClass() throws Exception {
        String source = "" +
        	"package org.carrot2.util.attribute.constraint;" +
        	"public class Test" +
        	"{" +
        	"static class AnnotationContainer" +
        	"{ @ValueHintEnum(values = TestValueSet.class) String hint; }" +
        	"public enum TestValueSet" +
        	"{ VALUE_1 }" +
        	"}";
        new JavaDocBuilder().addSource(new StringReader(source));
   }
    
    //for QDOX-153
    public void testAnotherEnumTest() throws Exception {
    	String source = "package org.apache.myfaces.el.unified.resolver;\n" +
    			"public final class FacesCompositeELResolver extends org.apache.myfaces.el.CompositeELResolver\n" +
    			"{\n" +
    			" public enum Scope\n" +
    			" { Faces, JSP }\n" +
    			" public FacesCompositeELResolver(final Scope scope) {}\n" +
    			"}";
    	
    	new JavaDocBuilder().addSource(new StringReader(source));
     }
}
