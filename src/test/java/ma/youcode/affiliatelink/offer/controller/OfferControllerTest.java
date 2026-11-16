package ma.youcode.affiliatelink.offer.controller;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.common.exception.OfferNotFoundException;
import ma.youcode.affiliatelink.offer.dto.request.CreateCampaignRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.OfferSearchCriteria;
import ma.youcode.affiliatelink.offer.dto.request.UpdateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.model.enums.PayoutModel;
import ma.youcode.affiliatelink.offer.service.CampaignService;
import ma.youcode.affiliatelink.offer.service.OfferService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OfferController.class, properties = "spring.docker.compose.enabled=false")
public class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfferService offerService;

    @MockitoBean
    private CampaignService campaignService;

    @Test
    void createOffer_shouldReturn201WithLocationHeader() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "advertiserId": 1,
                  "title": "Test Offer",
                  "description": "Test description",
                  "landingUrl": "https://test.com",
                  "niche": "TECH",
                  "tags": ["java"]
                }
                """;

        CreateOfferRequestDTO expectedDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                Set.of("java")
        );

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("Test Offer")
                .description("Test description")
                .landingUrl("https://test.com")
                .niche(Niche.TECH)
                .tags(Set.of("java"))
                .build();

        when(offerService.createOffer(expectedDTO)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/offers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Offer"))
                .andExpect(jsonPath("$.niche").value("TECH"));

        // Verify
        verify(offerService).createOffer(expectedDTO);
    }

    @Test
    void createOffer_shouldReturn400_whenBodyIsInvalid() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "advertiserId": -1,
                  "title": "",
                  "landingUrl": "not-a-url"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // Verify
        verifyNoInteractions(offerService, campaignService);
    }

    @Test
    void searchOffers_shouldPassCriteriaToService() throws Exception {

        // Arrange
        OfferSearchCriteria expectedCriteria = new OfferSearchCriteria(Niche.TECH, 1L, "java", "spring");

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("Spring Offer")
                .niche(Niche.TECH)
                .build();

        when(offerService.searchOffers(eq(expectedCriteria), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDTO)));

        // Act & Assert
        mockMvc.perform(get("/api/offers")
                        .param("niche", "TECH")
                        .param("advertiserId", "1")
                        .param("tag", "java")
                        .param("keyword", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Offer"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        // Verify
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(offerService).searchOffers(eq(expectedCriteria), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void searchOffers_shouldReturn400_whenNicheIsInvalid() throws Exception {

        // Act & Assert
        mockMvc.perform(get("/api/offers")
                        .param("niche", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request parameter"))
                .andExpect(jsonPath("$.detail").value("Invalid value 'UNKNOWN' for parameter 'niche'"));

        // Verify
        verifyNoInteractions(offerService);
    }

    @Test
    void getOfferById_shouldReturn200() throws Exception {

        // Arrange
        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("Test Offer")
                .niche(Niche.TECH)
                .build();

        when(offerService.getOfferById(1L)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Offer"));

        // Verify
        verify(offerService).getOfferById(1L);
    }

    @Test
    void getOfferById_shouldReturn404_whenOfferNotFound() throws Exception {

        // Arrange
        when(offerService.getOfferById(1L))
                .thenThrow(new OfferNotFoundException("Offer not found with id: 1"));

        // Act & Assert
        mockMvc.perform(get("/api/offers/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Offer not found"))
                .andExpect(jsonPath("$.detail").value("Offer not found with id: 1"));

        // Verify
        verify(offerService).getOfferById(1L);
    }

    @Test
    void updateOffer_shouldReturn200() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "title": "New Offer",
                  "description": "New description",
                  "landingUrl": "https://new.com",
                  "niche": "GAMING",
                  "tags": ["new"]
                }
                """;

        UpdateOfferRequestDTO expectedDTO = new UpdateOfferRequestDTO(
                "New Offer",
                "New description",
                "https://new.com",
                Niche.GAMING,
                Set.of("new")
        );

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("New Offer")
                .description("New description")
                .landingUrl("https://new.com")
                .niche(Niche.GAMING)
                .tags(Set.of("new"))
                .build();

        when(offerService.updateOffer(1L, expectedDTO)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/offers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Offer"))
                .andExpect(jsonPath("$.niche").value("GAMING"));

        // Verify
        verify(offerService).updateOffer(1L, expectedDTO);
    }

    @Test
    void createCampaign_shouldReturn201WithCampaignLocation() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "name": "Test Campaign",
                  "startDate": "2027-01-01",
                  "endDate": "2027-02-01",
                  "budget": 1000,
                  "payoutModel": "CPC",
                  "payoutValue": 2
                }
                """;

        CampaignResponseDTO responseDTO = CampaignResponseDTO.builder()
                .id(5L)
                .offerId(1L)
                .name("Test Campaign")
                .startDate(LocalDate.of(2027, 1, 1))
                .endDate(LocalDate.of(2027, 2, 1))
                .budget(BigDecimal.valueOf(1000))
                .payoutModel(PayoutModel.CPC)
                .payoutValue(BigDecimal.valueOf(2))
                .status(CampaignStatus.DRAFT)
                .build();

        when(campaignService.createCampaign(eq(1L), any(CreateCampaignRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/offers/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/campaigns/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.offerId").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        // Verify
        ArgumentCaptor<CreateCampaignRequestDTO> dtoCaptor = ArgumentCaptor.forClass(CreateCampaignRequestDTO.class);
        verify(campaignService).createCampaign(eq(1L), dtoCaptor.capture());

        CreateCampaignRequestDTO sentDTO = dtoCaptor.getValue();
        assertEquals("Test Campaign", sentDTO.name());
        assertEquals(LocalDate.of(2027, 1, 1), sentDTO.startDate());
        assertEquals(LocalDate.of(2027, 2, 1), sentDTO.endDate());
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(sentDTO.budget()));
        assertEquals(PayoutModel.CPC, sentDTO.payoutModel());
        assertEquals(0, BigDecimal.valueOf(2).compareTo(sentDTO.payoutValue()));
    }

    @Test
    void createCampaign_shouldReturn409_whenAdvertiserNotActive() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "name": "Test Campaign",
                  "startDate": "2027-01-01",
                  "endDate": "2027-02-01",
                  "budget": 1000,
                  "payoutModel": "CPC",
                  "payoutValue": 2
                }
                """;

        when(campaignService.createCampaign(eq(1L), any(CreateCampaignRequestDTO.class)))
                .thenThrow(new BusinessRuleException("Advertiser owning the offer must be active to create a campaign"));

        // Act & Assert
        mockMvc.perform(post("/api/offers/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business rule violation"))
                .andExpect(jsonPath("$.detail").value("Advertiser owning the offer must be active to create a campaign"));

        // Verify
        verify(campaignService).createCampaign(eq(1L), any(CreateCampaignRequestDTO.class));
    }
}
