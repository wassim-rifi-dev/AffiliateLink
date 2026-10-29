package ma.youcode.affiliatelink.offer.dto.request;

import ma.youcode.affiliatelink.common.enums.Niche;

public record OfferSearchCriteria(
        Niche niche,
        Long advertiserId,
        String tag,
        String keyword
) {}
