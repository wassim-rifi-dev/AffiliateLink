package ma.youcode.affiliatelink.features.profile.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.features.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.features.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.features.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profiles")
@RequiredArgsConstructor
public class AdminProfileController {

    private final ProfileService profileService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProfileStatusResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateProfileStatusRequestDTO dto) {
        ProfileStatusResponseDTO response = profileService.updateStatus(id, dto);
        
        return ResponseEntity.ok(response);
    }
}
