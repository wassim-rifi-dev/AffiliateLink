package ma.youcode.affiliatelink.offer.service;

import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.common.exception.OfferNotFoundException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.OfferSearchCriteria;
import ma.youcode.affiliatelink.offer.dto.request.UpdateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.mapper.OfferMapper;
import ma.youcode.affiliatelink.offer.model.entity.Offer;
import ma.youcode.affiliatelink.offer.model.entity.Tag;
import ma.youcode.affiliatelink.offer.repository.OfferRepository;
import ma.youcode.affiliatelink.offer.repository.OfferSpecifications;
import ma.youcode.affiliatelink.offer.repository.TagRepository;
import ma.youcode.affiliatelink.profile.ProfileApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OfferService {
    private final OfferRepository offerRepository;
    private final TagRepository tagRepository;
    private final OfferMapper offerMapper;
    private final ProfileApi profileApi;

    @Transactional
    public OfferResponseDTO createOffer(CreateOfferRequestDTO dto) {
        if (!profileApi.advertiserExists(dto.advertiserId())) {
            throw new ProfileNotExistException("Advertiser not found with id: " + dto.advertiserId());
        }

        if (!profileApi.isActiveAdvertiser(dto.advertiserId())) {
            throw new BusinessRuleException("Advertiser must be active to create an offer");
        }

        Offer offer = new Offer(dto.advertiserId(), dto.title(), dto.description(), dto.landingUrl(), dto.niche());
        offer.replaceTags(resolveTags(dto.tags()));

        Offer createdOffer = offerRepository.save(offer);

        return offerMapper.toResponseDTO(createdOffer);
    }

    @Transactional(readOnly = true)
    public OfferResponseDTO getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id: " + id));

        return offerMapper.toResponseDTO(offer);
    }

    @Transactional
    public OfferResponseDTO updateOffer(Long id, UpdateOfferRequestDTO dto) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id: " + id));

        offer.update(dto.title(), dto.description(), dto.landingUrl(), dto.niche());
        offer.replaceTags(resolveTags(dto.tags()));

        Offer updatedOffer = offerRepository.save(offer);

        return offerMapper.toResponseDTO(updatedOffer);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponseDTO> searchOffers(OfferSearchCriteria criteria, Pageable pageable) {
        List<Specification<Offer>> specs = new ArrayList<>();

        if (criteria.niche() != null) {
            specs.add(OfferSpecifications.hasNiche(criteria.niche()));
        }
        if (criteria.advertiserId() != null) {
            specs.add(OfferSpecifications.hasAdvertiser(criteria.advertiserId()));
        }
        if (criteria.tag() != null && !criteria.tag().isBlank()) {
            specs.add(OfferSpecifications.hasTag(criteria.tag()));
        }
        if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
            specs.add(OfferSpecifications.titleContains(criteria.keyword()));
        }

        return offerRepository.findAll(Specification.allOf(specs), pageable)
                .map(offerMapper::toResponseDTO);
    }

    private Set<Tag> resolveTags(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }

        Set<String> normalizedNames = names.stream()
                .map(Tag::normalize)
                .collect(Collectors.toSet());

        Set<Tag> tags = new HashSet<>(tagRepository.findByNameIn(normalizedNames));

        Set<String> existingNames = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        normalizedNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> tagRepository.save(new Tag(name)))
                .forEach(tags::add);

        return tags;
    }
}
