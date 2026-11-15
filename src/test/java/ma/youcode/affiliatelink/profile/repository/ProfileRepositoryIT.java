package ma.youcode.affiliatelink.profile.repository;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.entity.Profile;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
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
public class ProfileRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Advertiser buildAdvertiser(String email) {
        return Advertiser.builder()
                .email(email)
                .companyName("Test Company")
                .sector("Technology")
                .website("https://test.com")
                .build();
    }

    private Publisher buildPublisher(String email) {
        return Publisher.builder()
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech publisher")
                .country("Morocco")
                .build();
    }

    @Test
    void findById_shouldReturnAdvertiserInstance_whenProfileIsAdvertiser() {

        // Arrange
        Advertiser saved = advertiserRepository.saveAndFlush(buildAdvertiser("advertiser@email.com"));

        // Act
        Optional<Profile> result = profileRepository.findById(saved.getId());

        // Assert
        assertTrue(result.isPresent());
        assertInstanceOf(Advertiser.class, result.get());
        assertEquals("advertiser@email.com", result.get().getEmail());
        assertEquals("Test Company", ((Advertiser) result.get()).getCompanyName());
    }

    @Test
    void findById_shouldReturnPublisherInstance_whenProfileIsPublisher() {

        // Arrange
        Publisher saved = publisherRepository.saveAndFlush(buildPublisher("publisher@email.com"));

        // Act
        Optional<Profile> result = profileRepository.findById(saved.getId());

        // Assert
        assertTrue(result.isPresent());
        assertInstanceOf(Publisher.class, result.get());
        assertEquals("publisher@email.com", result.get().getEmail());
        assertEquals("John Doe", ((Publisher) result.get()).getUserFullName());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {

        // Act
        Optional<Profile> result = profileRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAdvertisersAndPublishers() {

        // Arrange
        advertiserRepository.saveAndFlush(buildAdvertiser("advertiser@email.com"));
        publisherRepository.saveAndFlush(buildPublisher("publisher@email.com"));

        // Act
        List<Profile> result = profileRepository.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.stream().filter(Advertiser.class::isInstance).count());
        assertEquals(1, result.stream().filter(Publisher.class::isInstance).count());
    }

    @Test
    void save_shouldThrow_whenEmailUsedByAnotherProfileType() {

        // Arrange
        advertiserRepository.saveAndFlush(buildAdvertiser("test@email.com"));
        Publisher duplicate = buildPublisher("test@email.com");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class,
                () -> publisherRepository.saveAndFlush(duplicate));
    }
}
