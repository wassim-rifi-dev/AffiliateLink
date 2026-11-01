package ma.youcode.affiliatelink.offer.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.model.enums.PayoutModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offer_campaigns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_model", nullable = false, length = 20)
    private PayoutModel payoutModel;

    @Column(name = "payout_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal payoutValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    public Campaign(String name, LocalDate startDate, LocalDate endDate,
                    BigDecimal budget, PayoutModel payoutModel, BigDecimal payoutValue) {
        if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget must be strictly positive");
        }
        if (payoutModel == null || !payoutModel.isValidValue(payoutValue)) {
            throw new IllegalArgumentException("Invalid payout value for model " + payoutModel);
        }
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.payoutModel = payoutModel;
        this.payoutValue = payoutValue;
        this.status = CampaignStatus.DRAFT;
    }

    public void transitionTo(CampaignStatus target, LocalDate today) {
        switch (target) {
            case ACTIVE -> activate(today);
            case PAUSED -> pause();
            case ENDED -> end();
            case DRAFT -> throw new InvalidStatusTransitionException(
                    "Invalid status transition: " + status + " -> " + target);
        }
    }

    public void activate(LocalDate today) {
        if (endDate.isBefore(today)) {
            throw new BusinessRuleException("Cannot activate a campaign whose end date has passed");
        }
        changeStatus(CampaignStatus.ACTIVE);
    }

    public void pause() {
        changeStatus(CampaignStatus.PAUSED);
    }

    public void end() {
        changeStatus(CampaignStatus.ENDED);
    }

    private void changeStatus(CampaignStatus target) {
        if (!status.canChangeStatus(target)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition: " + status + " -> " + target);
        }
        this.status = target;
    }
}
