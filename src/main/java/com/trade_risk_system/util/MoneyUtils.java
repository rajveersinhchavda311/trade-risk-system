package com.trade_risk_system.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Financial arithmetic constants and utility methods.
 * All monetary calculations in the system must use these constants
 * to ensure consistent precision and rounding behavior.
 */
public final class MoneyUtils {

    /** Scale for monetary values (prices, totals, exposures). */
    public static final int PRICE_SCALE = 4;

    /** Scale for ratio values (concentration risk, percentages). */
    public static final int RATIO_SCALE = 8;

    /** Standard rounding mode for financial calculations. */
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyUtils() {
        // Utility class — no instantiation
    }

    /**
     * Scales a monetary value to PRICE_SCALE decimal places.
     */
    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(PRICE_SCALE, ROUNDING);
    }

    /**
     * Scales a ratio value to RATIO_SCALE decimal places.
     */
    public static BigDecimal scaleRatio(BigDecimal value) {
        return value.setScale(RATIO_SCALE, ROUNDING);
    }
}
