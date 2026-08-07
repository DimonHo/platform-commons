package com.platformcommons.common.mask;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 脱敏序列化器：按 {@link Mask} 注解声明的类型脱敏后输出。
 */
public class MaskSerializer extends ValueSerializer<String> {

    private final MaskType type;

    public MaskSerializer(MaskType type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writeString(type.mask(value));
    }
}
