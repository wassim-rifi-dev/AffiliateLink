package ma.youcode.affiliatelink.features.profile.service;

import jakarta.transaction.Transactional;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.features.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.features.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.features.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {
    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Transactional
    public Publisher createPublisher(CreatePublisherRequestDTO dto) {
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

        return publisherRepository.save(publisher);
    }
}
