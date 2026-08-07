package com.platformcommons.common.mask;

/**
 * 脱敏类型。
 */
public enum MaskType {

    /** 手机号：保留前 3 位与后 4 位，如 138****8000。 */
    PHONE,
    /** 姓名：保留首字，如 张*。 */
    NAME,
    /** 证件号：保留前 4 位与后 4 位，如 1101********1234。 */
    ID_CARD,
    /** 银行卡号：仅保留末 4 位，如 ****1234。 */
    BANK_CARD;

    /**
     * 按类型脱敏。
     *
     * @param raw 原始值
     * @return 脱敏值（null 原样返回）
     */
    public String mask(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (this) {
            case PHONE -> maskRange(raw, 3, 4);
            case NAME -> raw.length() <= 1 ? "*" : raw.charAt(0) + "*".repeat(raw.length() - 1);
            case ID_CARD -> maskRange(raw, 4, 4);
            case BANK_CARD -> raw.length() <= 4 ? "****" : "****" + raw.substring(raw.length() - 4);
        };
    }

    /**
     * 保留前 head 位与后 tail 位，中间以星号填充。
     */
    private static String maskRange(String raw, int head, int tail) {
        if (raw.length() <= head + tail) {
            return "***";
        }
        return raw.substring(0, head) + "*".repeat(raw.length() - head - tail) + raw.substring(raw.length() - tail);
    }
}
