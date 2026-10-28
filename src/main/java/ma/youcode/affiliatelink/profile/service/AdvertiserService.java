package ma.youcode.affiliatelink.profile.service;

import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.mapper.AdvertiserMapper;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdvertiserService {
    private final AdvertiserRepository advertiserRepository;
    private final AdvertiserMapper advertiserMapper;

    @Transactional
    public AdvertiserResponseDTO createAdvertiser(CreateAdvertiserRequestDTO dto) {
        if (advertiserRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email already existe.");
        }

        Advertiser advertiser = advertiserMapper.toEntity(dto);

        Advertiser createdAdvertiser = advertiserRepository.save(advertiser);

        return advertiserMapper.toResponseDTO(createdAdvertiser);
    }

    public List<AdvertiserResponseDTO> getAllAdvertisers() {
        List<Advertiser> advertisers = advertiserRepository.findAll();

        return advertisers.stream()
                .map(advertiserMapper::toResponseDTO)
                .toList();
    }

    public AdvertiserResponseDTO getAdvertiserById(Long id) {
        Advertiser advertiser = advertiserRepository.findById(id)
                .orElseThrow(() -> new ProfileNotExistException("Advertiser not found with id: " + id));

        return advertiserMapper.toResponseDTO(advertiser);
    }

    public boolean isActiveAdvertiser(Long id) {
        Optional<Advertiser> advertiser = advertiserRepository.findById(id);

        if (advertiser.isEmpty() || advertiser.get().getStatus() != ProfileStatus.ACTIVE) {
            return false;
        }

        return true;
    }
}
