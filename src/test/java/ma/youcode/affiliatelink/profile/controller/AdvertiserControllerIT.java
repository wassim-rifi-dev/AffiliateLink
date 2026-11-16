package ma.youcode.affiliatelink.profile.controller;

import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class AdvertiserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    private static final String VALID_REQUEST_BODY = """
            {
              "email": "test@email.com",
              "companyName": "Test Company",
              "sector": "Technology",
              "website": "https://test.com"
            }
            """;

    private Advertiser saveAdvertiser(String email, String companyName) {
        return advertiserRepository.saveAndFlush(Advertiser.builder()
                .email(email)
                .companyName(companyName)
                .sector("Technology")
                .website("https://test.com")
                .status(ProfileStatus.PENDING)
                .build());
    }

    private String buildBody(String email, String companyName, String sector, String website) {
        return """
                {
                  "email": "%s",
                  "companyName": "%s",
                  "sector": "%s",
                  "website": "%s"
                }
                """.formatted(email, companyName, sector, website);
    }

    @Test
    void createAdvertiser_shouldReturn201WithLocationHeader() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("http://localhost/api/advertisers/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.sector").value("Technology"))
                .andExpect(jsonPath("$.website").value("https://test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createAdvertiser_shouldPersistAdvertiserInDatabase() throws Exception {

        // Act
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated());

        // Assert
        assertTrue(advertiserRepository.existsByEmail("test@email.com"));
        assertEquals(1, advertiserRepository.count());
    }

    @Test
    void createAdvertiser_shouldReturn409_whenEmailExists() throws Exception {

        // Arrange
        saveAdvertiser("test@email.com", "Other Company");

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already exists"));
    }

    @Test
    void createAdvertiser_shouldReturn400_whenEmailIsBlank() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("", "Test Company", "Technology", "https://test.com")))
                .andExpect(status().isBadRequest());

        assertEquals(0, advertiserRepository.count());
    }

    @Test
    void createAdvertiser_shouldReturn400_whenEmailIsInvalid() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("invalid-email", "Test Company", "Technology", "https://test.com")))
                .andExpect(status().isBadRequest());

        assertEquals(0, advertiserRepository.count());
    }

    @Test
    void createAdvertiser_shouldReturn400_whenCompanyNameIsBlank() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "", "Technology", "https://test.com")))
                .andExpect(status().isBadRequest());

        assertEquals(0, advertiserRepository.count());
    }

    @Test
    void createAdvertiser_shouldReturn400_whenCompanyNameTooShort() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "T", "Technology", "https://test.com")))
                .andExpect(status().isBadRequest());

        assertEquals(0, advertiserRepository.count());
    }

    @Test
    void createAdvertiser_shouldReturn400_whenSectorIsBlank() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "Test Company", "", "https://test.com")))
                .andExpect(status().isBadRequest());

        assertEquals(0, advertiserRepository.count());
    }
}
