package ma.youcode.affiliatelink.offer.dto.request;

import jakarta.validation.constraints.NotNull;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;

public record UpdateCampaignStatusRequestDTO(
        @NotNull(message = "Status is required")
        CampaignStatus status
) {}
