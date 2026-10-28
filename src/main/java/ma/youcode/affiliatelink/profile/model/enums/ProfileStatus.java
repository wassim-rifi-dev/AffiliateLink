package ma.youcode.affiliatelink.profile.model.enums;

public enum ProfileStatus {
    ACTIVE,
    PENDING,
    SUSPENDED;

    public boolean changeStatus(ProfileStatus target) {
        return switch (this) {
            case PENDING -> target == ACTIVE;
            case ACTIVE -> target == SUSPENDED;
            case SUSPENDED -> target == ACTIVE;
        };
    }
}
