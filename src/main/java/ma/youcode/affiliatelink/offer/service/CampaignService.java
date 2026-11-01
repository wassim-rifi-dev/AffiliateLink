package ma.youcode.affiliatelink.offer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.common.exception.CampaignNotFoundException;
import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
import ma.youcode.affiliatelink.common.exception.OfferNotFoundException;
import ma.youcode.affiliatelink.offer.dto.request.CreateCampaignRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.UpdateCampaignStatusRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.mapper.CampaignMapper;
import ma.youcode.affiliatelink.offer.model.entity.Campaign;
import ma.youcode.affiliatelink.offer.model.entity.Offer;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.repository.CampaignRepository;
import ma.youcode.affiliatelink.offer.repository.OfferRepository;
import ma.youcode.affiliatelink.profile.ProfileApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final OfferRepository offerRepository;
    private final CampaignMapper campaignMapper;
    private final ProfileApi profileApi;

    @Transactional
    public CampaignResponseDTO createCampaign(Long offerId, CreateCampaignRequestDTO dto) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found with id: " + offerId));

        if (!profileApi.isActiveAdvertiser(offer.getAdvertiserId())) {
            log.warn("Campaign creation rejected: advertiser not active, offerId={}, advertiserId={}",
                    offerId, offer.getAdvertiserId());
            throw new BusinessRuleException("Advertiser owning the offer must be active to create a campaign");
        }

        Campaign campaign = new Campaign(
                dto.name(), dto.startDate(), dto.endDate(),
                dto.budget(), dto.payoutModel(), dto.payoutValue()
        );
        offer.addCampaign(campaign);

        Campaign createdCampaign = campaignRepository.save(campaign);

        log.info("Campaign created: campaignId={}, offerId={}, status={}",
                createdCampaign.getId(), offerId, createdCampaign.getStatus());

        return campaignMapper.toResponseDTO(createdCampaign);
    }

    @Transactional
    public void updateStatus(Long id, UpdateCampaignStatusRequestDTO dto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with id: " + id));

        CampaignStatus previousStatus = campaign.getStatus();

        try {
            campaign.transitionTo(dto.status(), LocalDate.now());
        } catch (InvalidStatusTransitionException exception) {
            log.warn("Campaign status transition refused: campaignId={}, from={}, to={}",
                    id, previousStatus, dto.status());
            throw exception;
        } catch (BusinessRuleException exception) {
            log.warn("Campaign activation refused, end date passed: campaignId={}, endDate={}",
                    id, campaign.getEndDate());
            throw exception;
        }

        log.info("Campaign status changed: campaignId={}, from={}, to={}",
                id, previousStatus, campaign.getStatus());
    }

    @Transactional
    public void pauseActiveCampaignsOfAdvertiser(Long advertiserId) {
        List<Campaign> activeCampaigns =
                campaignRepository.findByOfferAdvertiserIdAndStatus(advertiserId, CampaignStatus.ACTIVE);

        activeCampaigns.forEach(Campaign::pause);

        log.info("Active campaigns paused for suspended advertiser: advertiserId={}, pausedCount={}",
                advertiserId, activeCampaigns.size());
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponseDTO> getCampaigns(CampaignStatus status, Pageable pageable) {
        log.debug("Listing campaigns: status={}, page={}, size={}, sort={}",
                status, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Campaign> campaigns = (status == null)
                ? campaignRepository.findAll(pageable)
                : campaignRepository.findByStatus(status, pageable);

        return campaigns.map(campaignMapper::toResponseDTO);
    }
}
