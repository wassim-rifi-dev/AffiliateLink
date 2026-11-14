package ma.youcode.affiliatelink.profile.controller;

import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.service.AdvertiserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdvertiserController.class, properties = "spring.docker.compose.enabled=false")
public class AdvertiserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdvertiserService advertiserService;

    private static final String VALID_REQUEST_BODY = """
            {
              "email": "test@email.com",
              "companyName": "Test Company",
              "sector": "Technology",
              "website": "https://test.com"
            }
            """;

    private static final CreateAdvertiserRequestDTO VALID_REQUEST_DTO = new CreateAdvertiserRequestDTO(
            "test@email.com",
            "Test Company",
            "Technology",
            "https://test.com"
    );

    @Test
    void createAdvertiser_shouldReturn201WithLocationHeader() throws Exception {

        // Arrange
        AdvertiserResponseDTO responseDTO = AdvertiserResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .status(ProfileStatus.PENDING)
                .companyName("Test Company")
                .sector("Technology")
                .website("https://test.com")
                .build();

        when(advertiserService.createAdvertiser(VALID_REQUEST_DTO)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/advertisers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify
        verify(advertiserService).createAdvertiser(VALID_REQUEST_DTO);
    }
}
