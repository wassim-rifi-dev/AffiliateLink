package ma.youcode.affiliatelink.features.profile.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.features.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.features.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.features.profile.model.entity.Profile;
import ma.youcode.affiliatelink.features.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public ProfileStatusResponseDTO updateStatus(Long id, UpdateProfileStatusRequestDTO dto) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ProfileNotExistException("Profile not found with id: " + id));

        profile.setStatus(dto.status());

        return ProfileStatusResponseDTO.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
