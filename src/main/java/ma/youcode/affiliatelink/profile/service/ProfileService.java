package ma.youcode.affiliatelink.profile.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.mapper.ProfileMapper;
import ma.youcode.affiliatelink.profile.AdvertiserSuspended;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.entity.Profile;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.ProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProfileStatusResponseDTO updateStatus(Long id, UpdateProfileStatusRequestDTO dto) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotExistException("Profile not found with id: " + id));

        profile.setStatus(dto.status());

        if (profile instanceof Advertiser && profile.getStatus() == ProfileStatus.SUSPENDED) {
            eventPublisher.publishEvent(new AdvertiserSuspended(profile.getId()));
        }

        return profileMapper.toStatusResponseDTO(profile);
    }
}
