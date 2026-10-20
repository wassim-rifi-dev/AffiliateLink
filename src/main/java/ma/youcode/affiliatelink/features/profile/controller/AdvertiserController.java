package ma.youcode.affiliatelink.features.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.features.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.features.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.features.profile.service.AdvertiserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/advertisers")
@RequiredArgsConstructor
public class AdvertiserController {
    private final AdvertiserService advertiserService;

    @PostMapping
    public ResponseEntity<AdvertiserResponseDTO> createAdvertiser(@Valid @RequestBody CreateAdvertiserRequestDTO dto) {
        AdvertiserResponseDTO created = advertiserService.createAdvertiser(dto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AdvertiserResponseDTO>> getAllAdvertisers() {
        List<AdvertiserResponseDTO> advertisers = advertiserService.getAllAdvertisers();

        return ResponseEntity.ok(advertisers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertiserResponseDTO> getAdvertiserById(@PathVariable Long id) {
        AdvertiserResponseDTO response = advertiserService.getAdvertiserById(id);

        return ResponseEntity.ok(response);
    }
}
