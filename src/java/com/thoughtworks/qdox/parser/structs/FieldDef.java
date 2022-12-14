package com.thoughtworks.qdox.parser.structs;

import java.util.HashSet;
import java.util.Set;

public class FieldDef extends LocatedDef {
    public String name = "";
    public TypeDef type;
    public Set modifiers = new HashSet();
    public int dimensions;
    public boolean isVarArgs;
    public String body = "";

    public boolean equals(Object obj) {
        FieldDef paramDef = (FieldDef) obj;
        boolean result = paramDef.name.equals(name)
                && paramDef.modifiers.equals(modifiers)
                && paramDef.isVarArgs == isVarArgs;
        if(paramDef.type == null) {
        	result &= (type == null)
        		&& paramDef.dimensions == dimensions;
        }
        else {
        	result &= (type != null)
        		&&(paramDef.type.name.equals(type.name))
        		&&(paramDef.type.actualArgumentTypes == null ? type.actualArgumentTypes == null: paramDef.type.actualArgumentTypes.equals(type.actualArgumentTypes))
        		&&(paramDef.type.dimensions + paramDef.dimensions == dimensions + type.dimensions);
        }
        return result;
    }

    public int hashCode() {
        return name.hashCode() + (type != null ? type.hashCode() : 0) +
                dimensions + modifiers.hashCode() + (isVarArgs ? 79769989 : 0);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(modifiers);
        result.append(' ');
        result.append(type);
        for (int i = 0; i < dimensions; i++) result.append("[]");
        result.append(' ');
        result.append(name);
        if(body.length() > 0){
            result.append(" = ").append(body);
        }
        return result.toString();
    }
}
