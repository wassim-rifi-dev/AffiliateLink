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
}
