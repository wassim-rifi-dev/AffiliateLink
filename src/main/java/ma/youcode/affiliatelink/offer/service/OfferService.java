package ma.youcode.affiliatelink.offer.service;

import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.OfferSearchCriteria;
import ma.youcode.affiliatelink.offer.dto.request.UpdateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferService {

    OfferResponseDTO createOffer(CreateOfferRequestDTO dto);

    OfferResponseDTO getOfferById(Long id);

    OfferResponseDTO updateOffer(Long id, UpdateOfferRequestDTO dto);

    Page<OfferResponseDTO> searchOffers(OfferSearchCriteria criteria, Pageable pageable);
}
