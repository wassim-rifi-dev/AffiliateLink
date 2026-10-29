package ma.youcode.affiliatelink.offer.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "offer_offers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertiser_id", nullable = false)
    private Long advertiserId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "landing_url", nullable = false)
    private String landingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Niche niche;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Campaign> campaigns = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @ManyToMany
    @JoinTable(
            name = "offer_offer_tags",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public Offer(Long advertiserId, String title, String description, String landingUrl, Niche niche) {
        this.advertiserId = advertiserId;
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, String description, String landingUrl, Niche niche) {
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.niche = niche;
    }

    public void replaceTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    public void addCampaign(Campaign campaign) {
        campaigns.add(campaign);
        campaign.setOffer(this);
    }

    public void activateCampaign(Campaign campaign, LocalDate today) {
        if (campaign.getOffer() != this) {
            throw new BusinessRuleException("The campaign does not belong to this offer");
        }
        campaigns.stream()
                .filter(c -> c != campaign && c.getStatus() == CampaignStatus.ACTIVE)
                .forEach(Campaign::pause);
        campaign.activate(today);
    }

    public List<Campaign> getCampaigns() {
        return Collections.unmodifiableList(campaigns);
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }
}
