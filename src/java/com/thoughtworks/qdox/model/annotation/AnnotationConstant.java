package com.thoughtworks.qdox.model.annotation;

import java.io.Serializable;

public class AnnotationConstant implements AnnotationValue, Serializable {

    private final Object value;

    private final String image;

    public AnnotationConstant( Object value, String image ) {
        this.value = value;
        this.image = image;
    }

    public Object getValue() {
        return value;
    }

    public String getImage() {
        return image;
    }

    public String toString() {
        return image;
    }

    public Object accept( AnnotationVisitor visitor ) {
        return visitor.visitAnnotationConstant( this );
    }

    public Object getParameterValue() {
        return image;
    }

}
