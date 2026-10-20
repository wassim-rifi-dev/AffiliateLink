package ma.youcode.affiliatelink.features.profile.repository;

import ma.youcode.affiliatelink.features.profile.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
