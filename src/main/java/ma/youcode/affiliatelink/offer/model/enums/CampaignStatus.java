package ma.youcode.affiliatelink.offer.model.enums;

public enum CampaignStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    ENDED;

    public boolean canChangeStatus(CampaignStatus target) {
        return switch (this) {
            case DRAFT -> target == ACTIVE;
            case ACTIVE -> target == PAUSED || target == ENDED;
            case PAUSED -> target == ACTIVE || target == ENDED;
            case ENDED -> false;
        };
    }
}
