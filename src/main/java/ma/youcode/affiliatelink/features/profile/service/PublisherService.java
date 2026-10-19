package ma.youcode.affiliatelink.features.profile.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.features.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.features.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.features.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.features.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PublisherService {
    private final PublisherRepository publisherRepository;

    @Transactional
    public PublisherResponseDTO createPublisher(CreatePublisherRequestDTO dto) {
        if (publisherRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email already existe.");
        }

        Publisher publisher = Publisher.builder()
                .email(dto.email())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .niche(dto.niche())
                .bio(dto.bio())
                .country(dto.country())
                .build();

        Publisher createdPublisher = publisherRepository.save(publisher);

        return PublisherResponseDTO.builder()
                .id(createdPublisher.getId())
                .email(createdPublisher.getEmail())
                .firstName(createdPublisher.getFirstName())
                .lastName(createdPublisher.getLastName())
                .niche(createdPublisher.getNiche())
                .bio(createdPublisher.getBio())
                .country(createdPublisher.getCountry())
                .status(createdPublisher.getStatus())
                .createdAt(createdPublisher.getCreatedAt())
                .build();
    }

    public List<PublisherResponseDTO> getAllPublisher() {
        List<Publisher> publishers = publisherRepository.findAll();

        List<PublisherResponseDTO> allPublisher = publishers.stream()
                .map(p -> PublisherResponseDTO.builder()
                        .id(p.getId())
                        .email(p.getEmail())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .niche(p.getNiche())
                        .bio(p.getBio())
                        .country(p.getCountry())
                        .status(p.getStatus())
                        .createdAt(p.getCreatedAt())
                        .build()
                )
                .toList();

        return allPublisher;
    }
}
