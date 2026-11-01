package ma.youcode.affiliatelink.offer.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ma.youcode.affiliatelink.offer.model.enums.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCampaignRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotNull(message = "Budget is required")
        @Positive(message = "Budget must be strictly positive")
        BigDecimal budget,

        @NotNull(message = "Payout model is required")
        PayoutModel payoutModel,

        @NotNull(message = "Payout value is required")
        @Positive(message = "Payout value must be strictly positive")
        BigDecimal payoutValue
) {
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Payout value must not exceed 100 for CPA_PERCENT")
    public boolean isPayoutValueValidForModel() {
        return payoutModel == null || payoutValue == null || payoutModel.isValidValue(payoutValue);
    }
}
