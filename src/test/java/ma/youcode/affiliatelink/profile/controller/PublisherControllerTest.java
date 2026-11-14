package ma.youcode.affiliatelink.profile.controller;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.service.PublisherService;
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

@WebMvcTest(controllers = PublisherController.class, properties = "spring.docker.compose.enabled=false")
public class PublisherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublisherService publisherService;

    private static final String VALID_REQUEST_BODY = """
            {
              "email": "test@email.com",
              "firstName": "John",
              "lastName": "Doe",
              "niche": "TECH",
              "bio": "Tech blogger",
              "country": "Morocco"
            }
            """;

    private static final CreatePublisherRequestDTO VALID_REQUEST_DTO = new CreatePublisherRequestDTO(
            "test@email.com",
            "John",
            "Doe",
            Niche.TECH,
            "Tech blogger",
            "Morocco"
    );

    @Test
    void createPublisher_shouldReturn201WithLocationHeader() throws Exception {

        // Arrange
        PublisherResponseDTO responseDTO = PublisherResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech blogger")
                .country("Morocco")
                .status(ProfileStatus.PENDING)
                .build();

        when(publisherService.createPublisher(VALID_REQUEST_DTO)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/publishers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.niche").value("TECH"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify
        verify(publisherService).createPublisher(VALID_REQUEST_DTO);
    }

    @Test
    void createPublisher_shouldReturn409_whenEmailExists() throws Exception {

        // Arrange
        when(publisherService.createPublisher(VALID_REQUEST_DTO))
                .thenThrow(new EmailAlreadyExistsException("Email already existe."));

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already exists"))
                .andExpect(jsonPath("$.detail").value("Email already existe."));

        // Verify
        verify(publisherService).createPublisher(VALID_REQUEST_DTO);
    }

    @Test
    void createPublisher_shouldReturn400_whenBodyIsInvalid() throws Exception {

        // Arrange
        String requestBody = """
                {
                  "email": "not-an-email",
                  "firstName": "",
                  "lastName": "Doe"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // Verify
        verifyNoInteractions(publisherService);
    }

    @Test
    void getAllPublisher_shouldReturn200() throws Exception {

        // Arrange
        PublisherResponseDTO responseDTO1 = PublisherResponseDTO.builder()
                .id(1L)
                .email("test1@email.com")
                .firstName("John")
                .build();

        PublisherResponseDTO responseDTO2 = PublisherResponseDTO.builder()
                .id(2L)
                .email("test2@email.com")
                .firstName("Jane")
                .build();

        when(publisherService.getAllPublisher()).thenReturn(List.of(responseDTO1, responseDTO2));

        // Act & Assert
        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        // Verify
        verify(publisherService).getAllPublisher();
    }
}
