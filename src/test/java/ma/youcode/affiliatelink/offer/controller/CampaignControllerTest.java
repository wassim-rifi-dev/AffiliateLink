package ma.youcode.affiliatelink.offer.controller;

import ma.youcode.affiliatelink.common.exception.CampaignNotFoundException;
import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
import ma.youcode.affiliatelink.offer.dto.request.UpdateCampaignStatusRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.CampaignResponseDTO;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CampaignController.class, properties = "spring.docker.compose.enabled=false")
public class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    @Test
    void getCampaigns_shouldReturn200WithPage() throws Exception {

        // Arrange
        CampaignResponseDTO responseDTO1 = CampaignResponseDTO.builder()
                .id(1L)
                .name("Campaign 1")
                .status(CampaignStatus.DRAFT)
                .build();

        CampaignResponseDTO responseDTO2 = CampaignResponseDTO.builder()
                .id(2L)
                .name("Campaign 2")
                .status(CampaignStatus.ACTIVE)
                .build();

        when(campaignService.getCampaigns(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDTO1, responseDTO2), PageRequest.of(0, 20), 2));

        // Act & Assert
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.number").value(0));

        // Verify
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(campaignService).getCampaigns(isNull(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("startDate").getDirection());
    }

    @Test
    void getCampaigns_shouldFilterByStatus() throws Exception {

        // Arrange
        CampaignResponseDTO responseDTO = CampaignResponseDTO.builder()
                .id(1L)
                .name("Active Campaign")
                .status(CampaignStatus.ACTIVE)
                .build();

        when(campaignService.getCampaigns(eq(CampaignStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDTO)));

        // Act & Assert
        mockMvc.perform(get("/api/campaigns")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        // Verify
        verify(campaignService).getCampaigns(eq(CampaignStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void updateStatus_shouldReturn204() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "status": "ACTIVE"
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/campaigns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify
        verify(campaignService).updateStatus(1L, new UpdateCampaignStatusRequestDTO(CampaignStatus.ACTIVE));
    }

    @Test
    void updateStatus_shouldReturn409_whenTransitionIsInvalid() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "status": "PAUSED"
                }
                """;

        UpdateCampaignStatusRequestDTO requestDTO = new UpdateCampaignStatusRequestDTO(CampaignStatus.PAUSED);

        doThrow(new InvalidStatusTransitionException("Invalid status transition: DRAFT -> PAUSED"))
                .when(campaignService).updateStatus(1L, requestDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/campaigns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid status transition"))
                .andExpect(jsonPath("$.detail").value("Invalid status transition: DRAFT -> PAUSED"));

        // Verify
        verify(campaignService).updateStatus(1L, requestDTO);
    }
}
