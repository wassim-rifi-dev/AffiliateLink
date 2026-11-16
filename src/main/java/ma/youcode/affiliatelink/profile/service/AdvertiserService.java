package ma.youcode.affiliatelink.profile.service;

import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;

import java.util.List;

public interface AdvertiserService {

    AdvertiserResponseDTO createAdvertiser(CreateAdvertiserRequestDTO dto);

    List<AdvertiserResponseDTO> getAllAdvertisers();

    AdvertiserResponseDTO getAdvertiserById(Long id);

    boolean advertiserExists(Long id);

    boolean isActiveAdvertiser(Long id);
}
