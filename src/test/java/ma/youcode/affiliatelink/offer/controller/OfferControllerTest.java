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
}
