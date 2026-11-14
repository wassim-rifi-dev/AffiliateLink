package ma.youcode.affiliatelink.profile.controller;

import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminProfileController.class, properties = "spring.docker.compose.enabled=false")
public class AdminProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void updateStatus_shouldReturn200() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "status": "ACTIVE"
                }
                """;

        UpdateProfileStatusRequestDTO requestDTO = new UpdateProfileStatusRequestDTO(ProfileStatus.ACTIVE);

        ProfileStatusResponseDTO responseDTO = ProfileStatusResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .status(ProfileStatus.ACTIVE)
                .build();

        when(profileService.updateStatus(1L, requestDTO)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify
        verify(profileService).updateStatus(1L, requestDTO);
    }

    @Test
    void updateStatus_shouldReturn404_whenProfileNotFound() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "status": "ACTIVE"
                }
                """;

        UpdateProfileStatusRequestDTO requestDTO = new UpdateProfileStatusRequestDTO(ProfileStatus.ACTIVE);

        when(profileService.updateStatus(1L, requestDTO))
                .thenThrow(new ProfileNotExistException("Profile not found with id: 1"));

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Profile not found"))
                .andExpect(jsonPath("$.detail").value("Profile not found with id: 1"));

        // Verify
        verify(profileService).updateStatus(1L, requestDTO);
    }
}
