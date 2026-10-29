package ma.youcode.affiliatelink.offer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.service.OfferService;
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
}
