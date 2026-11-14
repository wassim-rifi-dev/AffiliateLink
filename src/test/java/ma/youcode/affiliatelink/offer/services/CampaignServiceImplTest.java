package ma.youcode.affiliatelink.offer.services;

import ma.youcode.affiliatelink.common.enums.Niche;
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
import ma.youcode.affiliatelink.offer.model.enums.PayoutModel;
import ma.youcode.affiliatelink.offer.repository.CampaignRepository;
import ma.youcode.affiliatelink.offer.repository.OfferRepository;
import ma.youcode.affiliatelink.offer.service.impl.CampaignServiceImpl;
import ma.youcode.affiliatelink.profile.ProfileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CampaignMapper campaignMapper;

    @Mock
    private ProfileApi profileApi;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private static Campaign newCampaign(String name) {
        return new Campaign(
                name,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                BigDecimal.valueOf(1000),
                PayoutModel.CPC,
                BigDecimal.valueOf(2)
        );
    }

    @Test
    void createCampaign_shouldSaveAndReturnDto_whenAdvertiserIsActive() {

        // Arrange
        Offer offer = new Offer(1L, "Test Offer", "Test description", "https://test.com", Niche.TECH);

        CreateCampaignRequestDTO requestDTO = new CreateCampaignRequestDTO(
                "Test Campaign",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                BigDecimal.valueOf(1000),
                PayoutModel.CPC,
                BigDecimal.valueOf(2)
        );

        CampaignResponseDTO responseDTO = CampaignResponseDTO.builder()
                .id(1L)
                .offerId(1L)
                .name("Test Campaign")
                .startDate(requestDTO.startDate())
                .endDate(requestDTO.endDate())
                .budget(BigDecimal.valueOf(1000))
                .payoutModel(PayoutModel.CPC)
                .payoutValue(BigDecimal.valueOf(2))
                .status(CampaignStatus.DRAFT)
                .build();

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(true);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(campaignMapper.toResponseDTO(any(Campaign.class))).thenReturn(responseDTO);

        // Act
        CampaignResponseDTO result = campaignService.createCampaign(1L, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(campaignCaptor.capture());
        Campaign savedCampaign = campaignCaptor.getValue();

        assertEquals("Test Campaign", savedCampaign.getName());
        assertEquals(requestDTO.startDate(), savedCampaign.getStartDate());
        assertEquals(requestDTO.endDate(), savedCampaign.getEndDate());
        assertEquals(BigDecimal.valueOf(1000), savedCampaign.getBudget());
        assertEquals(PayoutModel.CPC, savedCampaign.getPayoutModel());
        assertEquals(BigDecimal.valueOf(2), savedCampaign.getPayoutValue());
        assertEquals(CampaignStatus.DRAFT, savedCampaign.getStatus());
        assertSame(offer, savedCampaign.getOffer());
        assertTrue(offer.getCampaigns().contains(savedCampaign));

        // Verify
        verify(offerRepository).findById(1L);
        verify(profileApi).isActiveAdvertiser(1L);
        verify(campaignMapper).toResponseDTO(savedCampaign);
    }

    @Test
    void createCampaign_shouldThrow_whenOfferNotFound() {

        // Arrange
        CreateCampaignRequestDTO requestDTO = new CreateCampaignRequestDTO(
                "Test Campaign",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                BigDecimal.valueOf(1000),
                PayoutModel.CPC,
                BigDecimal.valueOf(2)
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        OfferNotFoundException exception = assertThrows(
                OfferNotFoundException.class,
                () -> campaignService.createCampaign(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Offer not found with id: 1", exception.getMessage());

        // Verify
        verify(offerRepository).findById(1L);
        verifyNoInteractions(profileApi, campaignRepository, campaignMapper);
    }

    @Test
    void createCampaign_shouldThrow_whenAdvertiserIsNotActive() {

        // Arrange
        Offer offer = new Offer(1L, "Test Offer", "Test description", "https://test.com", Niche.TECH);

        CreateCampaignRequestDTO requestDTO = new CreateCampaignRequestDTO(
                "Test Campaign",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                BigDecimal.valueOf(1000),
                PayoutModel.CPC,
                BigDecimal.valueOf(2)
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(false);

        // Act
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> campaignService.createCampaign(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Advertiser owning the offer must be active to create a campaign", exception.getMessage());
        assertTrue(offer.getCampaigns().isEmpty());

        // Verify
        verify(offerRepository).findById(1L);
        verify(profileApi).isActiveAdvertiser(1L);
        verifyNoInteractions(campaignRepository, campaignMapper);
    }

    @Test
    void createCampaign_shouldThrow_whenDatesAreInvalid() {

        // Arrange
        Offer offer = new Offer(1L, "Test Offer", "Test description", "https://test.com", Niche.TECH);

        CreateCampaignRequestDTO requestDTO = new CreateCampaignRequestDTO(
                "Test Campaign",
                LocalDate.now().plusDays(30),
                LocalDate.now(),
                BigDecimal.valueOf(1000),
                PayoutModel.CPC,
                BigDecimal.valueOf(2)
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(true);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> campaignService.createCampaign(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("End date must be after start date", exception.getMessage());
        assertTrue(offer.getCampaigns().isEmpty());

        // Verify
        verify(offerRepository).findById(1L);
        verify(profileApi).isActiveAdvertiser(1L);
        verifyNoInteractions(campaignRepository, campaignMapper);
    }

    @Test
    void updateStatus_shouldChangeStatus_whenTransitionIsValid() {

        // Arrange
        Campaign campaign = newCampaign("Test Campaign");

        UpdateCampaignStatusRequestDTO requestDTO = new UpdateCampaignStatusRequestDTO(CampaignStatus.ACTIVE);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        // Act
        campaignService.updateStatus(1L, requestDTO);

        // Assert
        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());

        // Verify
        verify(campaignRepository).findById(1L);
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void updateStatus_shouldThrow_whenCampaignNotFound() {

        // Arrange
        UpdateCampaignStatusRequestDTO requestDTO = new UpdateCampaignStatusRequestDTO(CampaignStatus.ACTIVE);

        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        CampaignNotFoundException exception = assertThrows(
                CampaignNotFoundException.class,
                () -> campaignService.updateStatus(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Campaign not found with id: 1", exception.getMessage());

        // Verify
        verify(campaignRepository).findById(1L);
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void updateStatus_shouldThrow_whenTransitionIsInvalid() {

        // Arrange
        Campaign campaign = newCampaign("Test Campaign");

        UpdateCampaignStatusRequestDTO requestDTO = new UpdateCampaignStatusRequestDTO(CampaignStatus.PAUSED);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        // Act
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> campaignService.updateStatus(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Invalid status transition: DRAFT -> PAUSED", exception.getMessage());
        assertEquals(CampaignStatus.DRAFT, campaign.getStatus());

        // Verify
        verify(campaignRepository).findById(1L);
    }

    @Test
    void updateStatus_shouldThrow_whenActivatingWithPassedEndDate() {

        // Arrange
        Campaign campaign = new Campaign(
                "Expired Campaign",
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(1),
                BigDecimal.valueOf(1000),
                PayoutModel.CPC,
                BigDecimal.valueOf(2)
        );

        UpdateCampaignStatusRequestDTO requestDTO = new UpdateCampaignStatusRequestDTO(CampaignStatus.ACTIVE);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        // Act
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> campaignService.updateStatus(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Cannot activate a campaign whose end date has passed", exception.getMessage());
        assertEquals(CampaignStatus.DRAFT, campaign.getStatus());

        // Verify
        verify(campaignRepository).findById(1L);
    }

    @Test
    void pauseActiveCampaignsOfAdvertiser_shouldPauseAllActiveCampaigns() {

        // Arrange
        Campaign campaign1 = newCampaign("Campaign 1");
        Campaign campaign2 = newCampaign("Campaign 2");
        campaign1.activate(LocalDate.now());
        campaign2.activate(LocalDate.now());

        when(campaignRepository.findByOfferAdvertiserIdAndStatus(1L, CampaignStatus.ACTIVE))
                .thenReturn(List.of(campaign1, campaign2));

        // Act
        campaignService.pauseActiveCampaignsOfAdvertiser(1L);

        // Assert
        assertEquals(CampaignStatus.PAUSED, campaign1.getStatus());
        assertEquals(CampaignStatus.PAUSED, campaign2.getStatus());

        // Verify
        verify(campaignRepository).findByOfferAdvertiserIdAndStatus(1L, CampaignStatus.ACTIVE);
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void pauseActiveCampaignsOfAdvertiser_shouldDoNothing_whenNoActiveCampaigns() {

        // Arrange
        when(campaignRepository.findByOfferAdvertiserIdAndStatus(1L, CampaignStatus.ACTIVE))
                .thenReturn(List.of());

        // Act
        assertDoesNotThrow(() -> campaignService.pauseActiveCampaignsOfAdvertiser(1L));

        // Verify
        verify(campaignRepository).findByOfferAdvertiserIdAndStatus(1L, CampaignStatus.ACTIVE);
        verify(campaignRepository, never()).save(any(Campaign.class));
        verifyNoInteractions(offerRepository, campaignMapper, profileApi);
    }
}
