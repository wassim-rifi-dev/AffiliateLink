package ma.youcode.affiliatelink.offer.model.enums;

import java.math.BigDecimal;

public enum PayoutModel {
    CPC,
    CPL,
    CPA_FIXED,
    CPA_PERCENT;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public boolean isValidValue(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this != CPA_PERCENT || value.compareTo(HUNDRED) <= 0;
    }
}
