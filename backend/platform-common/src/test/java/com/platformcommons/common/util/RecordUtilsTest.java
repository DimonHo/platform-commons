package com.platformcommons.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link RecordUtils} 单元测试。
 */
class RecordUtilsTest {

    // 测试用源 record
    record Source(String name, int age, String email, List<String> tags) {}

    // 测试用目标 record——字段名和类型与 Source 完全一致
    record Target(String name, int age, String email, List<String> tags) {}

    // 目标 record 含 Source 中不存在的字段
    record TargetWithExtra(String name, int age, String email, List<String> tags, String extra) {}

    // 目标 record 只有 Source 的部分字段
    record TargetPartial(String name, int age) {}

    // 字段类型兼容：Set<String> → Collection（目标接收更宽类型不会工作，但同名同类型 OK）
    record SourceUser(String id, String role) {}
    record TargetUser(String id, String role) {}

    // 用于覆盖测试
    record Alert(String level, String code, String title) {}
    record AlertDto(String level, String code, String title) {}

    @Test
    void copy_sameFields() {
        var source = new Source("Alice", 30, "alice@test.com", List.of("a", "b"));

        Target result = RecordUtils.copy(source, Target.class);

        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals(30, result.age());
        assertEquals("alice@test.com", result.email());
        assertEquals(List.of("a", "b"), result.tags());
    }

    @Test
    void copy_nullSource() {
        assertNull(RecordUtils.copy(null, Target.class));
    }

    @Test
    void copy_extraFieldInTarget_defaultsNull() {
        var source = new Source("Bob", 25, "bob@test.com", List.of());

        TargetWithExtra result = RecordUtils.copy(source, TargetWithExtra.class);

        assertNotNull(result);
        assertEquals("Bob", result.name());
        assertEquals(25, result.age());
        assertNull(result.extra(), "目标多出的字段默认 null");
    }

    @Test
    void copy_partialTarget() {
        var source = new Source("Charlie", 40, "charlie@test.com", List.of("x"));

        TargetPartial result = RecordUtils.copy(source, TargetPartial.class);

        assertNotNull(result);
        assertEquals("Charlie", result.name());
        assertEquals(40, result.age());
    }

    @Test
    void copy_withOverrides() {
        var alert = new Alert("HIGH", null, "系统过载");

        AlertDto result = RecordUtils.copy(alert, AlertDto.class, Map.of(
                "code", "R-CAP-01"
        ));

        assertEquals("HIGH", result.level());
        assertEquals("R-CAP-01", result.code(), "override 值应覆盖源值");
        assertEquals("系统过载", result.title());
    }

    @Test
    void copy_withOverridesNull() {
        var alert = new Alert("LOW", "CODE-1", "测试");

        Map<String, Object> overrides = new java.util.HashMap<>();
        overrides.put("code", null);

        AlertDto result = RecordUtils.copy(alert, AlertDto.class, overrides);

        assertNull(result.code(), "override 显式 null 应生效");
    }

    @Test
    void copy_isCached() {
        var source = new SourceUser("u1", "ADMIN");

        // 连续两次调用，缓存生效
        TargetUser r1 = RecordUtils.copy(source, TargetUser.class);
        TargetUser r2 = RecordUtils.copy(source, TargetUser.class);

        assertNotSame(source, r1);
        assertEquals(r1, r2);
    }

    @Test
    void copy_nonRecordTarget_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                RecordUtils.copy(new Source("x", 1, "y", null), String.class)
        );
    }

    @Test
    void copy_emptyRecord() {
        record Empty() {}
        record EmptyTarget() {}

        EmptyTarget result = RecordUtils.copy(new Empty(), EmptyTarget.class);
        assertNotNull(result);
    }
}
