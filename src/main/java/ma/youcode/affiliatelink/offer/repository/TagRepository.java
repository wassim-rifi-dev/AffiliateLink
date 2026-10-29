package ma.youcode.affiliatelink.offer.repository;

import ma.youcode.affiliatelink.offer.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag , Long> {
}
