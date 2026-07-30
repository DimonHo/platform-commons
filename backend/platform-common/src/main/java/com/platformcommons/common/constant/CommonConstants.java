package com.platformcommons.common.constant;

/**
 * 公共常量类（禁止魔法值）。
 *
 * <p>阿里规范要求：代码中不允许出现未定义的魔法值，统一提取为常量。
 * 常量命名采用全大写 + 下划线分隔。</p>
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }

    // ===== 分页 =====
    /** 默认分页页码 */
    public static final int DEFAULT_PAGE_NUMBER = 1;
    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;
    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;

    // ===== 通用字段名 =====
    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    // ===== HTTP 头 =====
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    // ===== 编码与媒体类型 =====
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String MEDIA_TYPE_JSON = "application/json";

    // ===== 地理 =====
    /** 地球平均半径（米） */
    public static final double EARTH_RADIUS_METERS = 6_371_000.0D;

    // ===== 百分比 =====
    public static final int PERCENT_BASE = 100;

    // ===== 手机号脱敏 =====
    public static final int PHONE_MASK_PREFIX_LEN = 3;
    public static final int PHONE_MASK_SUFFIX_LEN = 4;
    public static final int PHONE_MIN_LEN = 7;
    public static final String PHONE_MASK = "****";

    // ===== 字符串 =====
    public static final String EMPTY_STRING = "";
    public static final String SEPARATOR_COMMA = ",";

    // ===== 业务标识 =====
    /** 状态：活跃 */
    public static final String STATUS_ACTIVE = "ACTIVE";
}
