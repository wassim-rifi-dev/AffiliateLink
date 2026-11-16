package ma.youcode.affiliatelink.profile.service;

import ma.youcode.affiliatelink.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;

public interface ProfileService {

    ProfileStatusResponseDTO updateStatus(Long id, UpdateProfileStatusRequestDTO dto);
}
