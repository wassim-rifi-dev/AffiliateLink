package ma.youcode.affiliatelink.offer.service;

import ma.youcode.affiliatelink.offer.dto.request.CreateCampaignRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.UpdateCampaignStatusRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampaignService {

    CampaignResponseDTO createCampaign(Long offerId, CreateCampaignRequestDTO dto);

    void updateStatus(Long id, UpdateCampaignStatusRequestDTO dto);

    void pauseActiveCampaignsOfAdvertiser(Long advertiserId);

    Page<CampaignResponseDTO> getCampaigns(CampaignStatus status, Pageable pageable);
}
