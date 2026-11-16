package ma.youcode.affiliatelink.profile.controller;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.PublisherRepository;
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
public class PublisherControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PublisherRepository publisherRepository;

    private static final String VALID_REQUEST_BODY = """
            {
              "email": "test@email.com",
              "firstName": "John",
              "lastName": "Doe",
              "niche": "TECH",
              "bio": "Tech publisher",
              "country": "Morocco"
            }
            """;

    private Publisher savePublisher(String email) {
        return publisherRepository.saveAndFlush(Publisher.builder()
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech publisher")
                .country("Morocco")
                .status(ProfileStatus.PENDING)
                .build());
    }

    private String buildBody(String email, String firstName, String lastName, String niche, String bio, String country) {
        return """
                {
                  "email": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "niche": %s,
                  "bio": "%s",
                  "country": "%s"
                }
                """.formatted(email, firstName, lastName, niche == null ? "null" : "\"" + niche + "\"", bio, country);
    }

    @Test
    void createPublisher_shouldReturn201WithLocationHeader() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("http://localhost/api/publishers/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.niche").value("TECH"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createPublisher_shouldPersistPublisherInDatabase() throws Exception {

        // Act
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated());

        // Assert
        assertTrue(publisherRepository.existsByEmail("test@email.com"));
        assertEquals(1, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn409_whenEmailExists() throws Exception {

        // Arrange
        savePublisher("test@email.com");

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already exists"));
    }

    @Test
    void createPublisher_shouldReturn400_whenEmailIsInvalid() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("invalid-email", "John", "Doe", "TECH", "Tech publisher", "Morocco")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenFirstNameIsBlank() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "", "Doe", "TECH", "Tech publisher", "Morocco")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenLastNameIsBlank() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "John", "", "TECH", "Tech publisher", "Morocco")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenNicheIsNull() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "John", "Doe", null, "Tech publisher", "Morocco")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenNicheIsUnknown() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "John", "Doe", "UNKNOWN", "Tech publisher", "Morocco")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenBioTooLong() throws Exception {

        // Arrange
        String longBio = "a".repeat(501);

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "John", "Doe", "TECH", longBio, "Morocco")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenCountryIsBlank() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildBody("test@email.com", "John", "Doe", "TECH", "Tech publisher", "")))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void createPublisher_shouldReturn400_whenBodyIsMissing() throws Exception {

        // Act & Assert
        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(0, publisherRepository.count());
    }

    @Test
    void getAllPublisher_shouldReturn200WithList() throws Exception {

        // Arrange
        savePublisher("first@email.com");
        savePublisher("second@email.com");

        // Act & Assert
        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].email").value(containsInAnyOrder(
                        "first@email.com", "second@email.com")));
    }

    @Test
    void getAllPublisher_shouldReturn200WithEmptyList_whenNoPublishers() throws Exception {

        // Act & Assert
        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
