package ma.youcode.affiliatelink.offer.repository;

import jakarta.persistence.criteria.Join;
import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.offer.model.entity.Offer;
import ma.youcode.affiliatelink.offer.model.entity.Tag;
import org.springframework.data.jpa.domain.Specification;

public final class OfferSpecifications {

    private OfferSpecifications() {
    }

    public static Specification<Offer> hasNiche(Niche niche) {
        return (root, query, cb) -> cb.equal(root.get("niche"), niche);
    }

    public static Specification<Offer> hasAdvertiser(Long advertiserId) {
        return (root, query, cb) -> cb.equal(root.get("advertiserId"), advertiserId);
    }

    public static Specification<Offer> hasTag(String tagName) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Offer, Tag> tags = root.join("tags");
            return cb.equal(tags.get("name"), Tag.normalize(tagName));
        };
    }

    public static Specification<Offer> titleContains(String keyword) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("title")),
                "%" + keyword.trim().toLowerCase() + "%"
        );
    }
}
