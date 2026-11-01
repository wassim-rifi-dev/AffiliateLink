package ma.youcode.affiliatelink.offer.repository;

import ma.youcode.affiliatelink.offer.model.entity.Campaign;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign , Long> {

    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

    List<Campaign> findByOfferAdvertiserIdAndStatus(Long advertiserId, CampaignStatus status);
}
