package com.platformcommons.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Record 拷贝工具——将 Spring {@code BeanUtils.copyProperties} 的能力扩展到 Java record。
 *
 * <p>Spring 的 {@code BeanUtils} 基于 getter/setter，无法写入不可变 record。
 * 本工具利用 {@link RecordComponent} 反射读取源 record 各字段值，
 * 按名称匹配目标 record 的规范构造器参数，一次性构造目标实例。</p>
 *
 * <p>典型用法（消除 Controller 中的 {@code toResponse} 样板代码）：</p>
 * <pre>{@code
 * // 之前
 * private DisputeResponse toResponse(Dispute d) {
 *     return new DisputeResponse(d.disputeId(), d.filedBy(), d.subject(), ...);
 * }
 *
 * // 之后
 * RecordUtils.copy(dispute, DisputeResponse.class);
 * }</pre>
 *
 * <p><b>约束</b>：目标 record 的每个组件必须在源 record 中存在同名且类型兼容的组件，
 * 否则该参数为 {@code null}（适用于可空字段）。构造器查找结果会缓存，反射开销仅首次。</p>
 */
public final class RecordUtils {

    private record ConstructorInfo(Constructor<?> constructor, String[] paramNames) {}

    /** 缓存：目标 record class → 规范构造器 + 参数名列表 */
    private static final Map<Class<?>, ConstructorInfo> CACHE = new ConcurrentHashMap<>();

    private RecordUtils() {
    }

    /**
     * 按字段名匹配拷贝 source record 到 target record 类型。
     *
     * @param source      源 record 实例
     * @param targetClass 目标 record 的 Class（必须有规范构造器）
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 目标 record 新实例
     * @throws IllegalArgumentException 若 source 非 record 或 targetClass 非 record
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T copy(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        if (!targetClass.isRecord()) {
            throw new IllegalArgumentException("目标类型必须是 record: " + targetClass.getName());
        }

        Object[] args = resolveArgs(source, targetClass);
        Constructor<?> ctor = getCachedInfo(targetClass).constructor();
        try {
            return (T) ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("构造 record 失败: " + targetClass.getName(), e);
        }
    }

    /**
     * 按字段名匹配拷贝，并允许对特定字段覆盖值。
     *
     * <p>用于大多数字段 1:1、个别字段需要转换的场景：</p>
     * <pre>{@code
     * RecordUtils.copy(alert, AlertResponse.class,
     *     Map.of("redLineCode", alert.redLine() == null ? null : alert.redLine().code()));
     * }</pre>
     *
     * @param source      源 record 实例
     * @param targetClass 目标 record 的 Class
     * @param overrides   字段名 → 覆盖值（优先于源 record 同名字段）
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 目标 record 新实例
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T copy(S source, Class<T> targetClass, Map<String, Object> overrides) {
        if (source == null) {
            return null;
        }
        if (!targetClass.isRecord()) {
            throw new IllegalArgumentException("目标类型必须是 record: " + targetClass.getName());
        }

        Object[] args = resolveArgs(source, targetClass, overrides);
        Constructor<?> ctor = getCachedInfo(targetClass).constructor();
        try {
            return (T) ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("构造 record 失败: " + targetClass.getName(), e);
        }
    }

    /**
     * 解析目标构造器参数值（无覆盖）。
     */
    private static <S> Object[] resolveArgs(S source, Class<?> targetClass) {
        return resolveArgs(source, targetClass, Map.of());
    }

    /**
     * 解析目标构造器参数值（支持覆盖）。
     */
    private static <S> Object[] resolveArgs(S source, Class<?> targetClass, Map<String, Object> overrides) {
        ConstructorInfo info = getCachedInfo(targetClass);

        // 源 record 字段名 → 值
        Map<String, Object> sourceValues = extractValues(source);

        String[] paramNames = info.paramNames();
        Object[] args = new Object[paramNames.length];
        for (int i = 0; i < paramNames.length; i++) {
            String name = paramNames[i];
            if (overrides.containsKey(name)) {
                args[i] = overrides.get(name);
            } else {
                args[i] = sourceValues.get(name);
            }
        }
        return args;
    }

    /**
     * 提取 record 所有组件值（通过 accessor 方法）。
     */
    private static Map<String, Object> extractValues(Object record) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        Map<String, Object> values = new HashMap<>(components.length * 2);
        try {
            for (RecordComponent rc : components) {
                values.put(rc.getName(), rc.getAccessor().invoke(record));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("读取 record 字段失败: " + record.getClass().getName(), e);
        }
        return values;
    }

    /**
     * 获取（或缓存）目标 record 的规范构造器及参数名列表。
     */
    private static ConstructorInfo getCachedInfo(Class<?> targetClass) {
        return CACHE.computeIfAbsent(targetClass, clazz -> {
            RecordComponent[] components = clazz.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            String[] paramNames = new String[components.length];
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
                paramNames[i] = components[i].getName();
            }
            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes);
                ctor.setAccessible(true);
                return new ConstructorInfo(ctor, paramNames);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "找不到 record 规范构造器: " + clazz.getName() +
                        ", paramTypes=" + Arrays.toString(paramTypes), e);
            }
        });
    }
}
