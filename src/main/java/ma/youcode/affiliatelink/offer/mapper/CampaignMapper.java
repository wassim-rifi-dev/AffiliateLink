package ma.youcode.affiliatelink.offer.mapper;

import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.model.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "offerId", source = "offer.id")
    CampaignResponseDTO toResponseDTO(Campaign campaign);
}
