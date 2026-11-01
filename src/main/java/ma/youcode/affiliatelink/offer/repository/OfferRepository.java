package ma.youcode.affiliatelink.offer.repository;

import ma.youcode.affiliatelink.offer.model.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfferRepository extends JpaRepository<Offer , Long>, JpaSpecificationExecutor<Offer> {
}
