package ma.youcode.affiliatelink.profile.services;

import jakarta.persistence.EntityManager;
import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.AdvertiserSuspended;
import ma.youcode.affiliatelink.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import ma.youcode.affiliatelink.profile.repository.ProfileRepository;
import ma.youcode.affiliatelink.profile.repository.PublisherRepository;
import ma.youcode.affiliatelink.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Testcontainers
@Transactional
@RecordApplicationEvents
public class ProfileServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ApplicationEvents applicationEvents;

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

    private ProfileStatus reloadStatus(Long id) {
        entityManager.flush();
        entityManager.clear();
        return profileRepository.findById(id).orElseThrow().getStatus();
    }

    @Test
    void updateStatus_shouldChangePendingToActive() {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.PENDING);

        // Act
        ProfileStatusResponseDTO result = profileService.updateStatus(
                saved.getId(), new UpdateProfileStatusRequestDTO(ProfileStatus.ACTIVE));

        // Assert
        assertEquals(saved.getId(), result.id());
        assertEquals(ProfileStatus.ACTIVE, result.status());
    }

    @Test
    void updateStatus_shouldChangeActiveToSuspended() {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.ACTIVE);

        // Act
        ProfileStatusResponseDTO result = profileService.updateStatus(
                saved.getId(), new UpdateProfileStatusRequestDTO(ProfileStatus.SUSPENDED));

        // Assert
        assertEquals(ProfileStatus.SUSPENDED, result.status());
    }
}
