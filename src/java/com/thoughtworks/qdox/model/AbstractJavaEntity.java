package com.thoughtworks.qdox.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractJavaEntity extends AbstractBaseJavaEntity implements Comparable {

    protected List modifiers = new ArrayList();
    private String comment;
    private DocletTag[] tags = new DocletTag[0];
    
    private JavaClass parentClass;
    /**
     * Return list of modifiers as Strings.
     * (public, private, protected, final, abstract, static)
     */
    public String[] getModifiers() {
        return (String[]) modifiers.toArray(new String[modifiers.size()]);
    }

    public String getComment() {
        return comment;
    }

    public DocletTag[] getTags() {
        return tags;
    }

    public DocletTag[] getTagsByName(String name) {
        List specifiedTags = new ArrayList();
        for (int i = 0; i < tags.length; i++) {
            DocletTag docletTag = tags[i];
            if (docletTag.getName().equals(name)) {
                specifiedTags.add(docletTag);
            }
        }
        return (DocletTag[]) specifiedTags.toArray(new DocletTag[specifiedTags.size()]);
    }

    public DocletTag getTagByName(String name) {
        for (int i = 0; i < tags.length; i++) {
            DocletTag docletTag = tags[i];
            if (docletTag.getName().equals(name)) {
                return docletTag;
            }
        }
        return null;
    }

    /**
     * Convenience method for <code>getTagByName(String).getNamedParameter(String)</code>
     * that also checks for null tag.
     * @since 1.3
     */
    public String getNamedParameter(String tagName, String parameterName) {
        DocletTag tag = getTagByName(tagName);
        if(tag != null) {
            return tag.getNamedParameter(parameterName);
        } else {
            return null;
        }
    }

    void commentHeader(IndentBuffer buffer) {
        if (comment == null && (tags == null || tags.length == 0)) {
            return;
        } else {
            buffer.write("/**");
            buffer.newline();

            if (comment != null && comment.length() > 0) {
                buffer.write(" * ");
                
                buffer.write(comment.replaceAll("\n", "\n * "));
                
                buffer.newline();
            }

            if (tags != null && tags.length > 0) {
                if (comment != null && comment.length() > 0) {
                    buffer.write(" *");
                    buffer.newline();
                }
                for (int i = 0; i < tags.length; i++) {
                    DocletTag docletTag = tags[i];
                    buffer.write(" * @");
                    buffer.write(docletTag.getName());
                    if (docletTag.getValue().length() > 0) {
                        buffer.write(' ');
                        buffer.write(docletTag.getValue());
                    }
                    buffer.newline();
                }
            }

            buffer.write(" */");
            buffer.newline();
        }
    }

    public String getCodeBlock() {
        IndentBuffer result = new IndentBuffer();
        write(result);
        return result.toString();
    }

    protected void write(IndentBuffer result) {
        commentHeader(result);
        writeBody(result);
    }

    protected abstract void writeBody(IndentBuffer result);

    public void setModifiers(String[] modifiers) {
        this.modifiers = Arrays.asList(modifiers);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTags(List tagList) {
        this.tags = new DocletTag[tagList.size()];
        tagList.toArray(this.tags);
    }

    //helper methods for querying the modifiers
    public boolean isAbstract() {
        return isModifierPresent("abstract");
    }

    public boolean isPublic() {
        return isModifierPresent("public");
    }

    public boolean isPrivate() {
        return isModifierPresent("private");
    }

    public boolean isProtected() {
        return isModifierPresent("protected");
    }

    public boolean isStatic() {
        return isModifierPresent("static");
    }

    public boolean isFinal() {
        return isModifierPresent("final");
    }

    public boolean isSynchronized() {
        return isModifierPresent("synchronized");
    }

    public boolean isTransient() {
        return isModifierPresent("transient");
    }

	/**
	 * @since 1.4
	 */
    public boolean isVolatile() {
        return isModifierPresent("volatile");
    }

	/**
	 * @since 1.4
	 */
    public boolean isNative() {
        return isModifierPresent("native");
    }

	/**
	 * @since 1.4
	 */
    public boolean isStrictfp() {
        return isModifierPresent("strictfp");
    }

    private boolean isModifierPresent(String modifier) {
        return modifiers.contains(modifier);
    }

    protected void writeNonAccessibilityModifiers(IndentBuffer result) {
        // modifiers (anything else)
        for (Iterator iter = modifiers.iterator(); iter.hasNext();) {
            String modifier = (String) iter.next();
            if (!modifier.startsWith("p")) {
                result.write(modifier);
                result.write(' ');
            }
        }
    }

    protected void writeAccessibilityModifier(IndentBuffer result) {
        for (Iterator iter = modifiers.iterator(); iter.hasNext();) {
            String modifier = (String) iter.next();
            if (modifier.startsWith("p")) {
                result.write(modifier);
                result.write(' ');
            }
        }
    }

    protected void writeAllModifiers(IndentBuffer result) {
        for (Iterator iter = modifiers.iterator(); iter.hasNext();) {
            String modifier = (String) iter.next();
            result.write(modifier);
            result.write(' ');
        }
    }
    
    public JavaSource getSource() { 
        return parentClass.getParentSource(); 
    }

    public void setParentClass( JavaClass parentClass )
    {
        this.parentClass = parentClass;
    }
    
    public JavaClass getParentClass()
    {
        return parentClass;
    }
}
