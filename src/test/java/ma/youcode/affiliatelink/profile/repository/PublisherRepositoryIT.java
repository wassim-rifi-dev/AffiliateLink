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
public class PublisherRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AdvertiserRepository advertiserRepository;

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
    void save_shouldPersistPublisherWithGeneratedId() {

        // Arrange
        Publisher publisher = buildPublisher("test@email.com");

        // Act
        Publisher result = publisherRepository.saveAndFlush(publisher);

        // Assert
        assertNotNull(result.getId());
        assertEquals("test@email.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(Niche.TECH, result.getNiche());
        assertEquals("Tech publisher", result.getBio());
        assertEquals("Morocco", result.getCountry());
    }

    @Test
    void save_shouldSetDefaultStatusPending_whenStatusNotProvided() {

        // Arrange
        Publisher publisher = buildPublisher("test@email.com");

        // Act
        Publisher result = publisherRepository.saveAndFlush(publisher);

        // Assert
        assertEquals(ProfileStatus.PENDING, result.getStatus());
    }

    @Test
    void save_shouldSetCreatedAt_whenPersisted() {

        // Arrange
        Publisher publisher = buildPublisher("test@email.com");

        // Act
        Publisher result = publisherRepository.saveAndFlush(publisher);

        // Assert
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void save_shouldThrow_whenEmailAlreadyExists() {

        // Arrange
        publisherRepository.saveAndFlush(buildPublisher("test@email.com"));
        Publisher duplicate = buildPublisher("test@email.com");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class,
                () -> publisherRepository.saveAndFlush(duplicate));
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {

        // Arrange
        publisherRepository.saveAndFlush(buildPublisher("test@email.com"));

        // Act
        boolean result = publisherRepository.existsByEmail("test@email.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotExists() {

        // Act
        boolean result = publisherRepository.existsByEmail("unknown@email.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void findById_shouldReturnPublisher_whenExists() {

        // Arrange
        Publisher saved = publisherRepository.saveAndFlush(buildPublisher("test@email.com"));

        // Act
        Optional<Publisher> result = publisherRepository.findById(saved.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals("test@email.com", result.get().getEmail());
        assertEquals("John Doe", result.get().getUserFullName());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {

        // Act
        Optional<Publisher> result = publisherRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }
}
