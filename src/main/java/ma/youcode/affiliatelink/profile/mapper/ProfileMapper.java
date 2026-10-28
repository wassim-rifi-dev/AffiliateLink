package ma.youcode.affiliatelink.profile.mapper;

import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileStatusResponseDTO toStatusResponseDTO(Profile profile) {
        return ProfileStatusResponseDTO.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
