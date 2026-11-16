package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.PublisherRepository;
import ma.youcode.affiliatelink.profile.service.PublisherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Testcontainers
@Transactional
public class PublisherServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private PublisherService publisherService;

    @Autowired
    private PublisherRepository publisherRepository;

    private CreatePublisherRequestDTO buildRequest(String email) {
        return new CreatePublisherRequestDTO(
                email,
                "John",
                "Doe",
                Niche.TECH,
                "Tech publisher",
                "Morocco"
        );
    }

    private Publisher savePublisher(String email) {
        return publisherRepository.saveAndFlush(Publisher.builder()
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech publisher")
                .country("Morocco")
                .build());
    }

    @Test
    void createPublisher_shouldPersistAndReturnResponse() {

        // Arrange
        CreatePublisherRequestDTO requestDTO = buildRequest("test@email.com");

        // Act
        PublisherResponseDTO result = publisherService.createPublisher(requestDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertNotNull(result.createdAt());
        assertEquals("test@email.com", result.email());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(Niche.TECH, result.niche());
        assertEquals("Tech publisher", result.bio());
        assertEquals("Morocco", result.country());
        assertTrue(publisherRepository.existsById(result.id()));
    }

    @Test
    void createPublisher_shouldReturnStatusPending() {

        // Arrange
        CreatePublisherRequestDTO requestDTO = buildRequest("test@email.com");

        // Act
        PublisherResponseDTO result = publisherService.createPublisher(requestDTO);

        // Assert
        assertEquals(ProfileStatus.PENDING, result.status());
    }

    @Test
    void createPublisher_shouldThrowEmailAlreadyExistsException_whenEmailExists() {

        // Arrange
        savePublisher("test@email.com");
        CreatePublisherRequestDTO requestDTO = buildRequest("test@email.com");

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class,
                () -> publisherService.createPublisher(requestDTO));
    }
}
