package ma.youcode.affiliatelink.offer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.OfferSearchCriteria;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.service.OfferService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<OfferResponseDTO> createOffer(@Valid @RequestBody CreateOfferRequestDTO dto) {
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
        OfferSearchCriteria criteria = new OfferSearchCriteria(niche, advertiserId, tag, keyword);

        return ResponseEntity.ok(new PagedModel<>(offerService.searchOffers(criteria, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> getOfferById(@PathVariable Long id) {
        OfferResponseDTO response = offerService.getOfferById(id);

        return ResponseEntity.ok(response);
    }
}
