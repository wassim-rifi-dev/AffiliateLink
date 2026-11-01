package ma.youcode.affiliatelink.offer.repository;

import ma.youcode.affiliatelink.offer.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag , Long> {
    List<Tag> findByNameIn(Collection<String> names);
}
