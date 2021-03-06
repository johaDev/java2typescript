/**
 * Copyright (c) CompuGROUP Software GmbH,
 * This software is the confidential and proprietary information of
 * CompuGROUP Software GmbH. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms of
 * the license agreement you entered into with CompuGROUP Software GmbH.
 */
package java2typescript.jackson.module.visitors;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import java2typescript.jackson.module.Configuration;
import java2typescript.jackson.module.grammar.EnumType;
import java2typescript.jackson.module.grammar.Module;
import java2typescript.jackson.module.grammar.base.AbstractNamedType;
import java2typescript.jackson.module.grammar.base.AbstractType;

import static java2typescript.jackson.module.grammar.base.CustomPrimitiveTypeProvider.CUSTOM_PRIMITIVE_TYPES;

public class TSJsonFormatVisitorWrapper extends ABaseTSJsonFormatVisitor implements
        JsonFormatVisitorWrapper {

    public TSJsonFormatVisitorWrapper(ABaseTSJsonFormatVisitor parentHolder, Configuration conf) {
        super(parentHolder, conf);
    }

    public TSJsonFormatVisitorWrapper(Module module, Configuration conf) {
        super(module, conf);
    }

    private <T extends ABaseTSJsonFormatVisitor<?>> T setTypeAndReturn(T actualVisitor) {
        type = actualVisitor.getType();
        return actualVisitor;
    }

    /**
     * Visit recursively the type, or return a cached response
     */
    public static AbstractType getTSTypeForHandler(ABaseTSJsonFormatVisitor<?> baseVisitor,
                                                   JsonFormatVisitable handler, JavaType typeHint, Configuration conf) throws JsonMappingException {

        AbstractType computedType = baseVisitor.getComputedTypes().get(typeHint);

        if (computedType != null) {
            return computedType;
        }
        TSJsonFormatVisitorWrapper visitor = new TSJsonFormatVisitorWrapper(baseVisitor, conf);
        handler.acceptJsonFormatVisitor(visitor, typeHint);
        baseVisitor.getComputedTypes().put(typeHint, visitor.getType());
        return visitor.getType();
    }

    /**
     * Either Java simple name or @JsonTypeName annotation
     */
    public String getName(JavaType type) {
        return conf.getNamingStrategy().getName(type);
    }

    private TSJsonObjectFormatVisitor useNamedClassOrParse(JavaType javaType) {

        String name = getName(javaType);

        AbstractNamedType namedType = getModule().resolveTypeName(name);

        if (CUSTOM_PRIMITIVE_TYPES.containsKey(name)) {
            type = CUSTOM_PRIMITIVE_TYPES.get(name);
            return null;
        } else if (namedType == null) {
            TSJsonObjectFormatVisitor visitor = new TSJsonObjectFormatVisitor(this, name, javaType
                    .getRawClass(), conf);
            type = visitor.getType();
            getModule().getNamedTypes().put(visitor.getType().getName(), visitor.getType());
            if (conf.getGeneratePublicMethods()) {
                visitor.addPublicMethods();
            }
            return visitor;
        } else {
            type = namedType;
            return null;
        }
    }

    public EnumType parseEnumOrGetFromCache(Module module, JavaType javaType) {
        String name = getName(javaType);
        AbstractType namedType = module.getNamedTypes().get(name);
        if (namedType == null) {
            EnumType enumType = new EnumType(name, module.getName());
            for (Object val : javaType.getRawClass().getEnumConstants()) {
                enumType.getValues().add(val.toString());
            }
            module.getNamedTypes().put(name, enumType);
            return enumType;
        } else {
            return (EnumType) namedType;
        }
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType type) throws JsonMappingException {
        return useNamedClassOrParse(type);
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType type) throws JsonMappingException {
        return setTypeAndReturn(new TSJsonArrayFormatVisitor(this, conf));
    }

    @Override
    public JsonStringFormatVisitor expectStringFormat(JavaType jType) throws JsonMappingException {
        if (jType.getRawClass().isEnum()) {
            type = parseEnumOrGetFromCache(getModule(), jType);
            return null;
        } else {
            return setTypeAndReturn(new TSJsonStringFormatVisitor(this, conf));
        }
    }

    @Override
    public JsonNumberFormatVisitor expectNumberFormat(JavaType type) throws JsonMappingException {
        return setTypeAndReturn(new TSJsonNumberFormatVisitor(this, conf));
    }

    @Override
    public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) throws JsonMappingException {
        return setTypeAndReturn(new TSJsonNumberFormatVisitor(this, conf));
    }

    @Override
    public JsonBooleanFormatVisitor expectBooleanFormat(JavaType type) throws JsonMappingException {
        return setTypeAndReturn(new TSJsonBooleanFormatVisitor(this, conf));
    }

    @Override
    public JsonNullFormatVisitor expectNullFormat(JavaType type) throws JsonMappingException {
        return setTypeAndReturn(new TSJsonNullFormatVisitor(this, conf));
    }

    @Override
    public JsonAnyFormatVisitor expectAnyFormat(JavaType type) throws JsonMappingException {
        if ("java.lang.Object".equals(type.getRawClass().getName())) {
            return setTypeAndReturn(new TSJsonAnyFormatVisitor(this, conf));
        }
        // probably just a class without fields/properties
        useNamedClassOrParse(type);
        return null;
    }

    @Override
    public JsonMapFormatVisitor expectMapFormat(JavaType type) throws JsonMappingException {
        return setTypeAndReturn(new TSJsonMapFormatVisitor(this, conf));
    }

}
