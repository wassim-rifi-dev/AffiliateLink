package ma.youcode.affiliatelink.offer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ma.youcode.affiliatelink.common.enums.Niche;

import java.util.Set;

public record CreateOfferRequestDTO(
        @NotNull(message = "Advertiser id is required")
        @Positive(message = "Advertiser id must be positive")
        Long advertiserId,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotBlank(message = "Landing URL is required")
        @Size(max = 255, message = "Landing URL must not exceed 255 characters")
        @Pattern(
                regexp = "^https?://([\\w-]+\\.)+[\\w-]{2,}(/\\S*)?$",
                message = "Landing URL must be a valid URL"
        )
        String landingUrl,

        @NotNull(message = "Niche is required")
        Niche niche,

        Set<@NotBlank(message = "Tag name must not be blank")
            @Size(max = 100, message = "Tag name must not exceed 100 characters") String> tags
) {}
