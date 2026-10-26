package ma.youcode.affiliatelink.profile.dto.response;

import lombok.Builder;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;

import java.time.LocalDateTime;

@Builder
public record ProfileStatusResponseDTO(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt
) {}