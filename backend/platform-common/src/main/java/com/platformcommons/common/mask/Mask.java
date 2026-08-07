package com.platformcommons.common.mask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感字段脱敏注解：标注后，Jackson 序列化时自动脱敏，DTO 内部保留原始值。
 *
 * <p>由 {@link MaskAnnotationIntrospector} 绑定到 {@link MaskSerializer}。</p>
 *
 * <pre>{@code
 * public record MemberResponse(
 *         ...
 *         @Mask(MaskType.PHONE) String phone,
 *         ...
 * ) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
public @interface Mask {

    /**
     * 脱敏类型。
     */
    MaskType value();
}
