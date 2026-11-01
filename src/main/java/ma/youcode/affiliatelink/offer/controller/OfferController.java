package ma.youcode.affiliatelink.offer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.offer.dto.request.CreateCampaignRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.OfferSearchCriteria;
import ma.youcode.affiliatelink.offer.dto.request.UpdateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.service.CampaignService;
import ma.youcode.affiliatelink.offer.service.OfferService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;
    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOffer(@Valid @RequestBody CreateOfferRequestDTO dto) {
        log.debug("POST /api/offers: advertiserId={}, niche={}", dto.advertiserId(), dto.niche());

        OfferResponseDTO created = offerService.createOffer(dto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping
    public ResponseEntity<PagedModel<OfferResponseDTO>> searchOffers(
            @RequestParam(required = false) Niche niche,
            @RequestParam(required = false) Long advertiserId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("GET /api/offers: niche={}, advertiserId={}, tag={}, keyword={}, page={}, size={}",
                niche, advertiserId, tag, keyword, pageable.getPageNumber(), pageable.getPageSize());

        OfferSearchCriteria criteria =new OfferSearchCriteria(niche, advertiserId, tag, keyword);

        return ResponseEntity.ok(new PagedModel<>(offerService.searchOffers(criteria, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable Long id) {
        log.debug("GET /api/offers/{}", id);

        OfferResponseDTO response = offerService.getOfferById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfferRequestDTO dto
    ) {
        log.debug("PUT /api/offers/{}", id);

        OfferResponseDTO updated = offerService.updateOffer(id, dto);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/campaigns")
    public ResponseEntity<CampaignResponseDTO> createCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CreateCampaignRequestDTO dto
    ) {
        log.debug("POST /api/offers/{}/campaigns: payoutModel={}", id, dto.payoutModel());

        CampaignResponseDTO created = campaignService.createCampaign(id, dto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/campaigns/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }
}
