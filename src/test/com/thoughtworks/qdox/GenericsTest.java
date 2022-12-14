package com.thoughtworks.qdox;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;

import junit.framework.TestCase;

import java.io.StringReader;

/**
 * @author <a href="mailto:joew@thoughtworks.com">Joe Walnes</a>
 * @author Aslak Helles&oslash;y
 * @author Mike Williams
 */
public class GenericsTest extends TestCase {
    
    private JavaDocBuilder builder = new JavaDocBuilder();

    public void testShouldUnderstandSingleGenericClassDeclarations() {
        String source = "" +
                "public interface Foo<T> extends Bar<T> {}";

        builder.addSource(new StringReader(source));
        assertEquals("Foo", builder.getClassByName("Foo").getName());
    }

    public void testShouldUnderstandMultipleGenericClassDeclarations() {
        String source = "" +
                "public interface Foo<X,Y> extends Bar<X,Y> {}";

        builder.addSource(new StringReader(source));
        assertEquals("Foo", builder.getClassByName("Foo").getName());
    }

    public void testShouldUnderstandMultipleGenericsInMethodDeclarations() {
        String source = "" +
                "public interface Foo {" +
                "   Bar<X,Y> zap(Zip<R,V> r);" +
                "}";

        builder.addSource(new StringReader(source));
        assertEquals("Foo", builder.getClassByName("Foo").getName());
    }

    public void testShouldUnderstandOuterAndInnerClassGenericsInMethodDeclarations() {
        String source = "" +
                "    public interface Foo {\n" +
                "       <A,B> void zap(Outer<A>.Inner<B> arg);\n" +
                "    }";

        builder.addSource(new StringReader(source));
        assertEquals("Foo", builder.getClassByName("Foo").getName());
    }

    public void testShouldUnderstandMultipleGenericsInConstructorDeclarations() {
        String source = "" +
                "public class Bar {" +
                "   public Bar(Zip<R,V> r) {}" +
                "}";

        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }

    public void testShouldUnderstandMultipleGenericsInFieldDeclarations() {
        String source = "" +
                "public class Bar {" +
                "   private Foo<R,V> foo;" +
                "}";

        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }
    
    public void testShouldUnderstandNestedGenerics() {
        String source = "" +
                "public class Bar {" +
                "   private List<List<Integer>> listOfLists;" +
                "}";

        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }
    
    public void testShouldUnderstandFullyQualifiedTypeArguments() {
        String source = "" +
                "public class Bar {" +
                "   private List<java.util.Date> listOfDates;" +
                "}";

        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }

    public void testShouldUnderstandBoundedTypeParameters() {
        String source = "" +
                "public class Bar<T extends Date> {}";

        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }

    public void testShouldUnderstandComplexBoundedTypeParameters() {
        String source = "" +
                "public class Bar<T extends Date & Serializable> {}";

        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }
    
    public void testShouldUnderstandWildcardTypeArguments() {
        String source = "" +
                "public class Bar { private Class<? extends Date> klass; }";
        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }
    
    public void testShouldUnderstandBoundedWildcardTypeArguments() {
        String source = "" +
                "public class Bar { Map<? super String, ? extends Date> klass; }";
        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }

    public void testShouldUnderstandMethodTypeParameters() {
        String source = "" +
                "public class Bar {" +
                "    public static <T extends Comparable<T>> T max(Collection<T> collection) {" +
                "        throw new UnsupportedOperationException();" +
                "    }" +
                "}";
        builder.addSource(new StringReader(source));
        assertEquals("Bar", builder.getClassByName("Bar").getName());
    }
    
    public void testGenericField() {
        // Also see QDOX-77
        String source = "" +
            "public class Foo {\n" +
            "    protected Map<String, Object> m_env = new HashMap();\n" +
            "    public Object retrieve(Class klass, Object key) {\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";
        builder.addSource(new StringReader(source));
        
        JavaClass fooClass = builder.getClassByName("Foo");
        assertNotNull(fooClass);
        assertEquals("Foo", fooClass.getName());
        
        JavaField envField = fooClass.getFieldByName("m_env");
        assertNotNull(envField);
        assertEquals("Map", envField.getType().getValue());
    } 

    public void testGenericFieldInstantiation() {
        // Also see QDOX-77
        String source = "" +
            "public class Foo {\n" +
            "    protected Map<String, Object> m_env = new HashMap<String, Object>();\n" +
            "    public Object retrieve(Class klass, Object key) {\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";
        builder.addSource(new StringReader(source));
        
        JavaClass fooClass = builder.getClassByName("Foo");
        assertNotNull(fooClass);
        assertEquals("Foo", fooClass.getName());
        
        JavaField envField = fooClass.getFieldByName("m_env");
        assertNotNull(envField);
        assertEquals("Map", envField.getType().getValue());
    } 

    public void testGenericFieldInstantiationHalfComplex() {
        // Also see QDOX-77
        String source = "" +
            "public class Foo {\n" +
            "    protected Map<Class<?>, Object> m_env = new HashMap<Class<?>, Object>();\n" +
            "    public Object retrieve(Class klass, Object key) {\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";
        builder.addSource(new StringReader(source));
        
        JavaClass fooClass = builder.getClassByName("Foo");
        assertNotNull(fooClass);
        assertEquals("Foo", fooClass.getName());
        
        JavaField envField = fooClass.getFieldByName("m_env");
        assertNotNull(envField);
        assertEquals("Map", envField.getType().getValue());
    }

    public void testGenericFieldInstantiationComplex() {
        // Also see QDOX-77
        String source = "" +
            "public class Foo {\n" +
            "    protected Map<Class<? extends Serializable>, Object> m_env = new HashMap<Class<? extends Serializable>, Object>();\n" +
            "    public Object retrieve(Class klass, Object key) {\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";
        builder.addSource(new StringReader(source));
        
        JavaClass fooClass = builder.getClassByName("Foo");
        assertNotNull(fooClass);
        assertEquals("Foo", fooClass.getName());
        
        JavaField envField = fooClass.getFieldByName("m_env");
        assertNotNull(envField);
        assertEquals("Map", envField.getType().getValue());
    }

    public void testGenericFieldInstantiationVeryComplex() {
        // Also see QDOX-77
        String source = "" +
            "public class Foo {\n" +
            "    protected Map<Map<Class<? extends Serializable>, ?>, Object> m_env = new HashMap<? extends Map<Class<? extends Serializable>, Object>, Object>();\n" +
            "    public Object retrieve(Class klass, Object key) {\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";
        builder.addSource(new StringReader(source));
        
        JavaClass fooClass = builder.getClassByName("Foo");
        assertNotNull(fooClass);
        assertEquals("Foo", fooClass.getName());
        
        JavaField envField = fooClass.getFieldByName("m_env");
        assertNotNull(envField);
        assertEquals("Map", envField.getType().getValue());
    }

    public void testJiraQdox66() {
        // Also see QDOX-77
        String source = "" +
            "public class Foo {\n" +
            "    protected Map<String, Object> m_env = new HashMap<String, Object>();\n" +
            "    public <T extends Object> T retrieve(Class<T> klass, Object key) {\n" +
            "        return x;\n" +
            "    }\n" +
            "}\n";
        builder.addSource(new StringReader(source));
        
        JavaClass fooClass = builder.getClassByName("Foo");
        assertNotNull(fooClass);
        assertEquals("Foo", fooClass.getName());
        
        JavaField envField = fooClass.getFieldByName("m_env");
        assertNotNull(envField);
        assertEquals("Map", envField.getType().getValue());
    } 
    
    // QDOX-207
    public void testMethodReturnTypeExtends() throws Exception {
        String superSource = "public abstract class Test<T> {\n" + 
        		"        private T me;\n" + 
        		"        public Test(T me) {\n" + 
        		"            this.me = me;\n" + 
        		"        }\n" + 
        		"        public T getValue() {\n" + 
        		"            return me;\n" + 
        		"        }\n" + 
        		"    }";
        String subSource = "public class StringTest extends Test<String> {\n" + 
        		"        public StringTest(String s) {\n" + 
        		"            super(s);\n" + 
        		"        }\n" + 
        		"    }";
        builder.addSource( new StringReader( superSource ) );
        builder.addSource( new StringReader( subSource ) );
        JavaMethod method = builder.getClassByName( "StringTest" ).getMethodBySignature( "getValue", null, true );
        assertEquals( "T", method.getGenericReturnType().getFullyQualifiedName() );
        assertEquals( "java.lang.Object", method.getReturnType().getFullyQualifiedName() );
        assertEquals( "java.lang.Object", method.getReturnType( false ).getFullyQualifiedName() );
        assertEquals( "java.lang.String", method.getReturnType( true ).getFullyQualifiedName() );
    }
    
    public void testMethodReturnTypeImplements() throws Exception {
        String source1="public interface GenericDao<TEntity, TKey> {\n" + 
        		"public List<TEntity> getAll();\n" + 
                "public TEntity getRandom();\n" + 
        		"public TEntity findById(TKey key);\n" + 
        		"public TEntity persist(TEntity entity);\n" + 
        		"public TEntity[] persist(TEntity[] entities);\n" + 
        		"public void delete(TEntity entity);\n" + 
        		"public Map<TKey, TEntity> asMap();" +
        		"}\r\n";
        String source2="public interface SubjectDao extends GenericDao<Subject, Long> {\n" + 
        		"public List<Subject> getEnabledSubjects();\n" + 
        		"}\r\n";
        String source3="public interface SubjectService extends RemoteService, SubjectDao {\r\n" + 
        		"}";
        builder.addSource( new StringReader( source1 ) );
        builder.addSource( new StringReader( source2 ) );
        builder.addSource( new StringReader( source3 ) );
        JavaMethod method = builder.getClassByName( "GenericDao" ).getMethodBySignature( "getRandom", null, true );
        assertEquals( "TEntity", method.getReturnType( true ).getGenericValue() );
        method = builder.getClassByName( "GenericDao" ).getMethodBySignature( "getAll", null, true );
        assertEquals( "List<TEntity>", method.getReturnType( true ).getGenericValue() );
        method = builder.getClassByName( "GenericDao" ).getMethodBySignature( "asMap", null, true );
        assertEquals( "Map<TKey,TEntity>", method.getReturnType( true ).getGenericValue() );

        method = builder.getClassByName( "SubjectDao" ).getMethodBySignature( "getRandom", null, true );
        assertEquals( "Subject", method.getReturnType( true ).getGenericValue() );
        method = builder.getClassByName( "SubjectDao" ).getMethodBySignature( "getAll", null, true );
        assertEquals( "List<Subject>", method.getReturnType( true ).getGenericValue() );
        method = builder.getClassByName( "SubjectDao" ).getMethodBySignature( "asMap", null, true );
        assertEquals( "Map<java.lang.Long,Subject>", method.getReturnType( true ).getGenericValue() );
        
        method = builder.getClassByName( "SubjectService" ).getMethodBySignature( "getRandom", null, true );
        assertEquals( "Subject", method.getReturnType( true ).getGenericValue() );
        method = builder.getClassByName( "SubjectService" ).getMethodBySignature( "getAll", null, true );
        assertEquals( "List<Subject>", method.getReturnType( true ).getGenericValue() );
        method = builder.getClassByName( "SubjectService" ).getMethodBySignature( "asMap", null, true );
        assertEquals( "Map<java.lang.Long,Subject>", method.getReturnType( true ).getGenericValue() );
    }
    
    //for QDOX-210
    public void testResolveTypeGetMethod() throws Exception {
        String source1="import java.util.*;" +
        "public interface GenericDao<TEntity, TKey> {\n" + 
        "public List<TEntity> getAll();\n" + 
        "public TEntity getRandom();\n" + 
        "public TEntity findById(TKey key);\n" + 
        "public TEntity persist(TEntity entity);\n" + 
        "public TEntity[] persist(TEntity[] entities);\n" + 
        "public void delete(TEntity entity);\n" + 
        "public Map<TKey, TEntity> asMap();" +
        "}\r\n";
        String source2="public interface SubjectDao extends GenericDao<Subject, Long> {\n" + 
        "}";
        String source3="public interface SubjectService extends RemoteService, SubjectDao {\r\n" + 
        "}";
        builder.addSource( new StringReader( source1 ) );
        builder.addSource( new StringReader( source2 ) );
        builder.addSource( new StringReader( source3 ) );
        
        JavaClass clazz = builder.getClassByName( "SubjectService" );
        JavaMethod method = clazz.getMethods( true )[0];
        assertEquals( "getAll", method.getName() );
        assertEquals( "java.util.List<Subject>", method.getReturnType( true ).getGenericValue() );
        method = clazz.getMethods( true )[2];
        assertEquals( "findById", method.getName() );
        assertEquals( "java.lang.Long", method.getParameterTypes( true )[0].getGenericValue() );
    }
    
    

}
