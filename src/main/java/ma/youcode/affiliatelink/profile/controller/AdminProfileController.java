package ma.youcode.affiliatelink.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.youcode.affiliatelink.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/profiles")
@RequiredArgsConstructor
public class AdminProfileController {

    private final ProfileService profileService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProfileStatusResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateProfileStatusRequestDTO dto) {
        log.debug("PATCH /api/admin/profiles/{}/status: targetStatus={}", id, dto.status());

        ProfileStatusResponseDTO response = profileService.updateStatus(id, dto);
        
        return ResponseEntity.ok(response);
    }
}
