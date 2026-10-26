package ma.youcode.affiliatelink.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.profile.service.PublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {
    private final PublisherService publisherService;

    @PostMapping
    public ResponseEntity<PublisherResponseDTO> createPublisher(@Valid @RequestBody CreatePublisherRequestDTO dto) {
        PublisherResponseDTO created = publisherService.createPublisher(dto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PublisherResponseDTO>> getAllPublisher() {
        List<PublisherResponseDTO> allPublishers = publisherService.getAllPublisher();

        return ResponseEntity.ok(allPublishers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> getPublisherById(@PathVariable("id") Long id) {
        PublisherResponseDTO publisher = publisherService.getPublisherById(id);

        return ResponseEntity.ok(publisher);
    }
}
