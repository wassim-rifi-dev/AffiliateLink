package ma.youcode.affiliatelink.profile.repository;

import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertiserRepository extends JpaRepository<Advertiser , Long> {
    boolean existsByEmail(String email);
}
