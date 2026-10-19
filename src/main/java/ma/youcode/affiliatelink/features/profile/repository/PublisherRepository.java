package ma.youcode.affiliatelink.features.profile.repository;

import ma.youcode.affiliatelink.features.profile.model.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher , Long> {
    boolean existsByEmail(String email);
}
