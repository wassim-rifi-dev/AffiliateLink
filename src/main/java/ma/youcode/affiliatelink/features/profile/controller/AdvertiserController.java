package ma.youcode.affiliatelink.features.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.features.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.features.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.features.profile.service.AdvertiserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
}
