package ma.youcode.affiliatelink.features.profile.repository;

import ma.youcode.affiliatelink.features.profile.model.entity.Advertiser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertiserRepository extends JpaRepository<Advertiser , Long> {
    boolean existsByEmail(String email);
}
