package ma.youcode.affiliatelink.offer.dto.response;

import lombok.Builder;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.model.enums.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CampaignResponseDTO(
        Long id,
        Long offerId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        PayoutModel payoutModel,
        BigDecimal payoutValue,
        CampaignStatus status
) {}
