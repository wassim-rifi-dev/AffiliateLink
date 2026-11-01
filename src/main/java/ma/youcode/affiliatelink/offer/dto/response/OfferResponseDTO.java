package ma.youcode.affiliatelink.offer.dto.response;

import lombok.Builder;
import ma.youcode.affiliatelink.common.enums.Niche;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record OfferResponseDTO(
        Long id,
        Long advertiserId,
        String title,
        String description,
        String landingUrl,
        Niche niche,
        LocalDateTime createdAt,
        Set<String> tags
) {}
