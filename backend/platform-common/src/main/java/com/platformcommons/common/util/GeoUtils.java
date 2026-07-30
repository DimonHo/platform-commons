package com.platformcommons.common.util;

import com.platformcommons.common.constant.CommonConstants;

/**
 * 地理计算工具。
 *
 * <p>使用 Haversine 公式计算两点间球面距离，适用于短距离网约车/配送场景。</p>
 */
public final class GeoUtils {

    private GeoUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    private static final double HALF = 0.5D;
    private static final double TWO = 2.0D;

    /**
     * 使用 Haversine 公式计算两经纬度之间的球面距离。
     *
     * @param lat1 起点纬度（度）
     * @param lon1 起点经度（度）
     * @param lat2 终点纬度（度）
     * @param lon2 终点经度（度）
     * @return 距离（米）
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double sinLat = Math.sin(latDistance / TWO);
        double sinLon = Math.sin(lonDistance / TWO);
        double a = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLon * sinLon;
        double c = TWO * Math.atan2(Math.sqrt(a), Math.sqrt(1.0D - a));
        return CommonConstants.EARTH_RADIUS_METERS * c;
    }
}
