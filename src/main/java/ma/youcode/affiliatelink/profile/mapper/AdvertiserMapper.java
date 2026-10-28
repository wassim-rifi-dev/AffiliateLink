package ma.youcode.affiliatelink.profile.mapper;

import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdvertiserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Advertiser toEntity(CreateAdvertiserRequestDTO dto);

    AdvertiserResponseDTO toResponseDTO(Advertiser advertiser);
}
