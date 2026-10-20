package ma.youcode.affiliatelink.features.profile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAdvertiserRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Company name is required")
        @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
        String companyName,

        @NotBlank(message = "Sector is required")
        @Size(max = 100, message = "Sector must not exceed 100 characters")
        String sector,

        @Size(max = 255, message = "Website must not exceed 255 characters")
        @Pattern(
                regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]{2,}(/\\S*)?$",
                message = "Website must be a valid URL"
        )
        String website
) {}
