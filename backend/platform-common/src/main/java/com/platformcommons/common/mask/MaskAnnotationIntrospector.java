package com.platformcommons.common.mask;

import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * 注解内省器：将 {@link Mask} 注解绑定到 {@link MaskSerializer}。
 *
 * <p>继承 {@link JacksonAnnotationIntrospector} 保留标准注解处理，仅追加脱敏逻辑。</p>
 */
public class MaskAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public Object findSerializer(MapperConfig<?> config, Annotated a) {
        Mask mask = a.getAnnotation(Mask.class);
        if (mask != null) {
            return new MaskSerializer(mask.value());
        }
        return super.findSerializer(config, a);
    }
}
