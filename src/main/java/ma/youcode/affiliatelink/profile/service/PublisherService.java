package ma.youcode.affiliatelink.profile.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.profile.mapper.PublisherMapper;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Transactional
    public PublisherResponseDTO createPublisher(CreatePublisherRequestDTO dto) {
        if (publisherRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email already existe.");
        }

        Publisher publisher = publisherMapper.toEntity(dto);

        Publisher createdPublisher = publisherRepository.save(publisher);

        return publisherMapper.toResponseDTO(createdPublisher);
    }

    public List<PublisherResponseDTO> getAllPublisher() {
        List<Publisher> publishers = publisherRepository.findAll();

        return publishers.stream()
                .map(publisherMapper::toResponseDTO)
                .toList();
    }

    public PublisherResponseDTO getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ProfileNotExistException("Publisher not found with id: " + id));

        return publisherMapper.toResponseDTO(publisher);
    }
}
