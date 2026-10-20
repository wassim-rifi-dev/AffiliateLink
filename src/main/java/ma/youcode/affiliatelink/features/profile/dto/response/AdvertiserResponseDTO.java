package ma.youcode.affiliatelink.features.profile.dto.response;

import ma.youcode.affiliatelink.features.profile.model.enums.ProfileStatus;

import java.time.LocalDateTime;

public record AdvertiserResponseDTO(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String companyName,
        String sector,
        String website
) {}
