package ma.youcode.affiliatelink.profile.mapper;

import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import org.springframework.stereotype.Component;

@Component
public class AdvertiserMapper {

    public Advertiser toEntity(CreateAdvertiserRequestDTO dto) {
        return Advertiser.builder()
                .email(dto.email())
                .companyName(dto.companyName())
                .sector(dto.sector())
                .website(dto.website())
                .build();
    }

    public AdvertiserResponseDTO toResponseDTO(Advertiser advertiser) {
        return AdvertiserResponseDTO.builder()
                .id(advertiser.getId())
                .email(advertiser.getEmail())
                .companyName(advertiser.getCompanyName())
                .sector(advertiser.getSector())
                .website(advertiser.getWebsite())
                .status(advertiser.getStatus())
                .createdAt(advertiser.getCreatedAt())
                .build();
    }
}
