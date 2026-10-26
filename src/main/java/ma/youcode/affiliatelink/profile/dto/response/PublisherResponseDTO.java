package ma.youcode.affiliatelink.profile.dto.response;

import lombok.Builder;
import ma.youcode.affiliatelink.profile.model.enums.Niche;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;

import java.time.LocalDateTime;

@Builder
public record PublisherResponseDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        Niche niche,
        String bio,
        String country,
        ProfileStatus status,
        LocalDateTime createdAt
) {}
