package ma.youcode.affiliatelink.features.profile.dto.response;

import lombok.Builder;
import ma.youcode.affiliatelink.features.profile.model.enums.ProfileStatus;

import java.time.LocalDateTime;

@Builder
public record ProfileStatusResponseDTO(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt
) {}