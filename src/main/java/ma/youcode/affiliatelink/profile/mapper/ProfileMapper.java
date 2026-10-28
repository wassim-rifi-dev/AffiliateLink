package ma.youcode.affiliatelink.profile.mapper;

import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileStatusResponseDTO toStatusResponseDTO(Profile profile);
}
