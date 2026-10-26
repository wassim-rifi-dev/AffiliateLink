package ma.youcode.affiliatelink.profile.dto.request;

import jakarta.validation.constraints.NotNull;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;

public record UpdateProfileStatusRequestDTO(
        @NotNull(message = "Status is required")
        ProfileStatus status
) {}
