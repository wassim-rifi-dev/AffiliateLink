package ma.youcode.affiliatelink.profile.mapper;

import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {

    public Publisher toEntity(CreatePublisherRequestDTO dto) {
        return Publisher.builder()
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .niche(dto.niche())
                .bio(dto.bio())
                .country(dto.country())
                .build();
    }

    public PublisherResponseDTO toResponseDTO(Publisher publisher) {
        return PublisherResponseDTO.builder()
                .id(publisher.getId())
                .email(publisher.getEmail())
                .firstName(publisher.getFirstName())
                .lastName(publisher.getLastName())
                .niche(publisher.getNiche())
                .bio(publisher.getBio())
                .country(publisher.getCountry())
                .status(publisher.getStatus())
                .createdAt(publisher.getCreatedAt())
                .build();
    }
}
