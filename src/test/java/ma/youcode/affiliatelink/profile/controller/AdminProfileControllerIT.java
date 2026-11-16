package ma.youcode.affiliatelink.profile.controller;

import jakarta.persistence.EntityManager;
import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import ma.youcode.affiliatelink.profile.repository.ProfileRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class AdminProfileControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private EntityManager entityManager;

    private Advertiser saveAdvertiser(ProfileStatus status) {
        return advertiserRepository.saveAndFlush(Advertiser.builder()
                .email("advertiser@email.com")
                .companyName("Test Company")
                .sector("Technology")
                .website("https://test.com")
                .status(status)
                .build());
    }

    private Publisher savePublisher(ProfileStatus status) {
        return publisherRepository.saveAndFlush(Publisher.builder()
                .email("publisher@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech publisher")
                .country("Morocco")
                .status(status)
                .build());
    }

    private String statusBody(String status) {
        return """
                {
                  "status": %s
                }
                """.formatted(status == null ? "null" : "\"" + status + "\"");
    }

    private ProfileStatus reloadStatus(Long id) {
        entityManager.flush();
        entityManager.clear();
        return profileRepository.findById(id).orElseThrow().getStatus();
    }

    @Test
    void updateStatus_shouldReturn200_whenPendingToActive() throws Exception {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.PENDING);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody("ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.email").value("advertiser@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateStatus_shouldReturn200_whenActiveToSuspended() throws Exception {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.ACTIVE);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody("SUSPENDED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void updateStatus_shouldReturn200_whenSuspendedToActive() throws Exception {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.SUSPENDED);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody("ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateStatus_shouldPersistNewStatusInDatabase() throws Exception {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.PENDING);

        // Act
        mockMvc.perform(patch("/api/admin/profiles/{id}/status", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody("ACTIVE")))
                .andExpect(status().isOk());

        // Assert
        assertEquals(ProfileStatus.ACTIVE, reloadStatus(saved.getId()));
    }
}
