package ma.youcode.affiliatelink.profile.repository;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class AdvertiserRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Advertiser buildAdvertiser(String email, String companyName) {
        return Advertiser.builder()
                .email(email)
                .companyName(companyName)
                .sector("Technology")
                .website("https://test.com")
                .build();
    }

    @Test
    void save_shouldPersistAdvertiserWithGeneratedId() {

        // Arrange
        Advertiser advertiser = buildAdvertiser("test@email.com", "Test Company");

        // Act
        Advertiser result = advertiserRepository.saveAndFlush(advertiser);

        // Assert
        assertNotNull(result.getId());
        assertEquals("test@email.com", result.getEmail());
        assertEquals("Test Company", result.getCompanyName());
        assertEquals("Technology", result.getSector());
        assertEquals("https://test.com", result.getWebsite());
    }

    @Test
    void save_shouldSetDefaultStatusPending_whenStatusNotProvided() {

        // Arrange
        Advertiser advertiser = buildAdvertiser("test@email.com", "Test Company");

        // Act
        Advertiser result = advertiserRepository.saveAndFlush(advertiser);

        // Assert
        assertEquals(ProfileStatus.PENDING, result.getStatus());
    }

    @Test
    void save_shouldSetCreatedAt_whenPersisted() {

        // Arrange
        Advertiser advertiser = buildAdvertiser("test@email.com", "Test Company");

        // Act
        Advertiser result = advertiserRepository.saveAndFlush(advertiser);

        // Assert
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void save_shouldThrow_whenEmailAlreadyExists() {

        // Arrange
        advertiserRepository.saveAndFlush(buildAdvertiser("test@email.com", "Test Company"));
        Advertiser duplicate = buildAdvertiser("test@email.com", "Other Company");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class,
                () -> advertiserRepository.saveAndFlush(duplicate));
    }

    @Test
    void save_shouldThrow_whenCompanyNameAlreadyExists() {

        // Arrange
        advertiserRepository.saveAndFlush(buildAdvertiser("test@email.com", "Test Company"));
        Advertiser duplicate = buildAdvertiser("other@email.com", "Test Company");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class,
                () -> advertiserRepository.saveAndFlush(duplicate));
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {

        // Arrange
        advertiserRepository.saveAndFlush(buildAdvertiser("test@email.com", "Test Company"));

        // Act
        boolean result = advertiserRepository.existsByEmail("test@email.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotExists() {

        // Act
        boolean result = advertiserRepository.existsByEmail("unknown@email.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void findById_shouldReturnAdvertiser_whenExists() {

        // Arrange
        Advertiser saved = advertiserRepository.saveAndFlush(buildAdvertiser("test@email.com", "Test Company"));

        // Act
        Optional<Advertiser> result = advertiserRepository.findById(saved.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals("test@email.com", result.get().getEmail());
        assertEquals("Test Company", result.get().getCompanyName());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {

        // Act
        Optional<Advertiser> result = advertiserRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnOnlyAdvertisers() {

        // Arrange
        advertiserRepository.saveAndFlush(buildAdvertiser("advertiser@email.com", "Test Company"));

        publisherRepository.saveAndFlush(Publisher.builder()
                .email("publisher@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech publisher")
                .country("Morocco")
                .build());

        // Act
        List<Advertiser> result = advertiserRepository.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("advertiser@email.com", result.get(0).getEmail());
    }
}
