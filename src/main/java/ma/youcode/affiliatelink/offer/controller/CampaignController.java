package ma.youcode.affiliatelink.offer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.youcode.affiliatelink.offer.dto.request.UpdateCampaignStatusRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.service.CampaignService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<PagedModel<CampaignResponseDTO>> getCampaigns(
            @RequestParam(required = false) CampaignStatus status,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("GET /api/campaigns: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(new PagedModel<>(campaignService.getCampaigns(status, pageable)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCampaignStatusRequestDTO dto
    ) {
        log.debug("PATCH /api/campaigns/{}/status: targetStatus={}", id, dto.status());

        campaignService.updateStatus(id, dto);

        return ResponseEntity.noContent().build();
    }
}
