package com.platformcommons.common.mask;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Mask} 注解序列化测试。
 *
 * <p>验证：{@code @Mask} 字段经 {@link MaskAnnotationIntrospector} 绑定的 ObjectMapper 序列化时自动脱敏，
 * DTO 内部仍保留原始值；null 安全；无注解字段不受影响；标准 Jackson 注解（如 {@code @JsonProperty}）不受影响。</p>
 */
class MaskTest {

    private record Profile(
            @Mask(MaskType.PHONE) String phone,
            @Mask(MaskType.NAME) String name,
            @Mask(MaskType.ID_CARD) String idCard,
            @Mask(MaskType.BANK_CARD) String bankCard,
            String raw,
            @Mask(MaskType.PHONE) String nullPhone
    ) {
    }

    private record Renamed(@JsonProperty("custom_name") @Mask(MaskType.NAME) String name) {
    }

    private final JsonMapper mapper = JsonMapper.builder()
            .annotationIntrospector(new MaskAnnotationIntrospector())
            .build();

    @Test
    void maskTypes_produceExpectedPatterns() {
        assertEquals("138****8000", MaskType.PHONE.mask("13800138000"));
        assertEquals("张*", MaskType.NAME.mask("张三"));
        assertEquals("1101**********1234", MaskType.ID_CARD.mask("110101199001011234"));
        assertEquals("****2234", MaskType.BANK_CARD.mask("6222020200112234"));
        assertNull(MaskType.PHONE.mask(null), "null 原样返回");
        assertEquals("***", MaskType.PHONE.mask("123"), "过短值返回 ***");
        assertEquals("*", MaskType.NAME.mask("单"), "单字姓名保留 *");
    }

    @Test
    void annotatedFields_areMaskedOnSerialization() throws Exception {
        Profile p = new Profile("13800138000", "张三", "110101199001011234", "6222020200112234", "plain", null);

        String json = mapper.writeValueAsString(p);

        assertEquals("""
                {"phone":"138****8000","name":"张*","idCard":"1101**********1234","bankCard":"****2234","raw":"plain","nullPhone":null}""",
                json, "@Mask 字段序列化时脱敏，无注解字段原样，null 输出 null");
    }

    @Test
    void accessor_stillReturnsRawValue() {
        Profile p = new Profile("13800138000", null, null, null, null, null);
        assertEquals("13800138000", p.phone(), "DTO 内部保留原始值，脱敏仅发生在序列化边界");
    }

    @Test
    void standardAnnotations_stillWorkAlongsideMask() throws Exception {
        String json = mapper.writeValueAsString(new Renamed("李四"));
        assertEquals("{\"custom_name\":\"李*\"}", json,
                "@JsonProperty 重命名与 @Mask 脱敏可共存（继承 JacksonAnnotationIntrospector 保留标准行为）");
    }
}
