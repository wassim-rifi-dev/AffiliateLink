package ma.youcode.affiliatelink.profile.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdvertiserService {
    private final AdvertiserRepository advertiserRepository;

    @Transactional
    public AdvertiserResponseDTO createAdvertiser(CreateAdvertiserRequestDTO dto) {
        if (advertiserRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Email already existe.");
        }

        Advertiser advertiser = Advertiser.builder()
                .email(dto.email())
                .companyName(dto.companyName())
                .sector(dto.sector())
                .website(dto.website())
                .build();

        Advertiser createdAdvertiser = advertiserRepository.save(advertiser);

        return AdvertiserResponseDTO.builder()
                .id(createdAdvertiser.getId())
                .email(createdAdvertiser.getEmail())
                .companyName(createdAdvertiser.getCompanyName())
                .sector(createdAdvertiser.getSector())
                .website(createdAdvertiser.getWebsite())
                .status(createdAdvertiser.getStatus())
                .createdAt(createdAdvertiser.getCreatedAt())
                .build();
    }

    public List<AdvertiserResponseDTO> getAllAdvertisers() {
        List<Advertiser> advertisers = advertiserRepository.findAll();

        return advertisers.stream()
                .map(a -> AdvertiserResponseDTO.builder()
                        .id(a.getId())
                        .email(a.getEmail())
                        .companyName(a.getCompanyName())
                        .sector(a.getSector())
                        .website(a.getWebsite())
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .build()
                )
                .toList();
    }

    public AdvertiserResponseDTO getAdvertiserById(Long id) {
        Advertiser advertiser = advertiserRepository.findById(id)
                .orElseThrow(() -> new ProfileNotExistException("Advertiser not found with id: " + id));

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

    public boolean isActiveAdvertiser(Long id) {
        Optional<Advertiser> advertiser = advertiserRepository.findById(id);

        if (advertiser.isEmpty() || advertiser.get().getStatus() != ProfileStatus.ACTIVE) {
            return false;
        }

        return true;
    }
}
