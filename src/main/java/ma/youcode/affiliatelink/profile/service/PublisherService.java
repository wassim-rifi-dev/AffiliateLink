package ma.youcode.affiliatelink.profile.service;

import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;

import java.util.List;

public interface PublisherService {

    PublisherResponseDTO createPublisher(CreatePublisherRequestDTO dto);

    List<PublisherResponseDTO> getAllPublisher();

    PublisherResponseDTO getPublisherById(Long id);
}
