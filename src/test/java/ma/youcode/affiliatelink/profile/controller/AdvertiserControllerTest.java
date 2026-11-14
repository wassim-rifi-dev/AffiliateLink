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

    @Test
    void createAdvertiser_shouldReturn409_whenEmailExists() throws Exception {

        // Arrange
        when(advertiserService.createAdvertiser(VALID_REQUEST_DTO))
                .thenThrow(new EmailAlreadyExistsException("Email already existe."));

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already exists"))
                .andExpect(jsonPath("$.detail").value("Email already existe."));

        // Verify
        verify(advertiserService).createAdvertiser(VALID_REQUEST_DTO);
    }

    @Test
    void createAdvertiser_shouldReturn400_whenBodyIsInvalid() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "email": "not-an-email",
                  "companyName": "",
                  "website": "not a url"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // Verify
        verifyNoInteractions(advertiserService);
    }

    @Test
    void getAllAdvertisers_shouldReturn200() throws Exception {

        // Arrange
        AdvertiserResponseDTO responseDTO1 = AdvertiserResponseDTO.builder()
                .id(1L)
                .email("test1@email.com")
                .companyName("Test Company 1")
                .build();

        AdvertiserResponseDTO responseDTO2 = AdvertiserResponseDTO.builder()
                .id(2L)
                .email("test2@email.com")
                .companyName("Test Company 2")
                .build();

        when(advertiserService.getAllAdvertisers()).thenReturn(List.of(responseDTO1, responseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/advertisers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Test Company 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].companyName").value("Test Company 2"));

        // Verify
        verify(advertiserService).getAllAdvertisers();
    }

    @Test
    void getAdvertiserById_shouldReturn200() throws Exception {

        // Arrange
        AdvertiserResponseDTO responseDTO = AdvertiserResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .status(ProfileStatus.ACTIVE)
                .companyName("Test Company")
                .build();

        when(advertiserService.getAdvertiserById(1L)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/advertisers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify
        verify(advertiserService).getAdvertiserById(1L);
    }
}
