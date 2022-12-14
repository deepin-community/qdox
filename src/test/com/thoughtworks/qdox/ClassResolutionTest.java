package com.thoughtworks.qdox;

import java.io.StringReader;

import junit.framework.TestCase;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

public class ClassResolutionTest extends TestCase {

    public void testNestedClassesResolvedAcrossPackageBoundaries() {

        // input sources
        String source1 = ""
                + "package package1;"
                + "public class Class1 {"
                + " public static final class NestedClass {}"
                + "}";

        String source2 = ""
                + "package package2;"
                + "import package1.Class1;"
                + "public class Class2 {"
                + " public void doStuff(Class1.NestedClass arg) {}"
                + "}";

        // parse
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSource(new StringReader(source1));
        builder.addSource(new StringReader(source2));

        // find the parameter
        JavaClass class2 = builder.getClassByName("package2.Class2");
        JavaMethod method = class2.getMethods()[0];
        JavaParameter parameter = method.getParameters()[0];
        Type type = parameter.getType();

        // verify
        assertEquals("Should include fully qualified name", "package1.Class1$NestedClass", type.getFullyQualifiedName());
    }

    public void testSurvivesStaticImports() {

        // input sources
        String source = ""
                + "package package2;"
                + "import static package1.Class1.VALUE;"
                + "public class Class2 {"
                + " public void doStuff(String arg) {}"
                + "}";

        // parse
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSource(new StringReader(source));

        // find the parameter
        JavaClass class2 = builder.getClassByName("package2.Class2");
        assertNotNull(class2);
    }

    public void testAnonymousClass() {
        JavaDocBuilder builder = new JavaDocBuilder();

    	String source = ""
        	+ "public class AnimatedAlgorithm {\n"
        	+ "    private SelectionListener mySelectionListener = new SelectionListenerAdapter() {\n"
        	+ "        public void selectionEvent() {\n"
        	+ "            for (int i = 0; i < recalcers.size(); i++) {\n"
        	+ "              int something = 5;"
        	+ "            }\n"
        	+ "        }\n"
        	+ "    };\n"
        	+ "}";

        builder.addSource(new StringReader(source));
    }
    
    //from QDOX-86
    public void testInnerClassInMethod() throws Exception {
    	JavaDocBuilder builder = new JavaDocBuilder();
    	String source = "package some.pack;\n" +
    	"class Test {\n" +
    	"void some(Inner.Inner2 a) {}\n" +
    	"static interface Inner {\n" +
    	"static interface Inner2 { }\n" +
    	"}\n" +
    	"}";
    	builder.addSource(new StringReader(source));
    	JavaMethod method = builder.getClassByName("some.pack.Test").getMethods()[0];
    	JavaParameter parameter = method.getParameters()[0];
    	assertEquals("some.pack.Test$Inner$Inner2", parameter.getType().getJavaClass().getFullyQualifiedName());
    }
}
