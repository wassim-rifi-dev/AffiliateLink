package ma.youcode.affiliatelink.profile.repository;

import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher , Long> {
    boolean existsByEmail(String email);
}
