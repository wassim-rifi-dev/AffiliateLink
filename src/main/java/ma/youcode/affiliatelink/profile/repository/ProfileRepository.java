package ma.youcode.affiliatelink.profile.repository;

import ma.youcode.affiliatelink.profile.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
