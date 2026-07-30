package com.platformcommons.common;

/**
 * 分页查询参数。
 *
 * @param pageNumber 页码（从 1 开始）
 * @param pageSize   每页大小
 */
public record PageQuery(int pageNumber, int pageSize) {

    /** 默认页码。 */
    public static final int DEFAULT_PAGE_NUMBER = 1;
    /** 默认每页大小。 */
    public static final int DEFAULT_PAGE_SIZE = 20;
    /** 最大每页大小。 */
    public static final int MAX_PAGE_SIZE = 200;

    /**
     * 紧凑构造器：校验并修正非法分页参数。
     */
    public PageQuery {
        if (pageNumber < 1) {
            pageNumber = DEFAULT_PAGE_NUMBER;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }

    /**
     * 计算偏移量。
     *
     * @return 偏移量
     */
    public long offset() {
        return (long) (pageNumber - 1) * pageSize;
    }
}
