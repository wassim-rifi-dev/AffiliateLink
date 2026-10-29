package ma.youcode.affiliatelink.offer.mapper;

import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.model.entity.Offer;
import ma.youcode.affiliatelink.offer.model.entity.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OfferMapper {

    OfferResponseDTO toResponseDTO(Offer offer);

    default String toTagName(Tag tag) {
        return tag.getName();
    }
}
