package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import ma.youcode.affiliatelink.profile.service.AdvertiserService;
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
public class AdvertiserServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AdvertiserService advertiserService;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    private CreateAdvertiserRequestDTO buildRequest(String email, String companyName) {
        return new CreateAdvertiserRequestDTO(
                email,
                companyName,
                "Technology",
                "https://test.com"
        );
    }

    private Advertiser saveAdvertiser(String email, String companyName, ProfileStatus status) {
        return advertiserRepository.saveAndFlush(Advertiser.builder()
                .email(email)
                .companyName(companyName)
                .sector("Technology")
                .website("https://test.com")
                .status(status)
                .build());
    }

    @Test
    void createAdvertiser_shouldPersistAndReturnResponse() {

        // Arrange
        CreateAdvertiserRequestDTO requestDTO = buildRequest("test@email.com", "Test Company");

        // Act
        AdvertiserResponseDTO result = advertiserService.createAdvertiser(requestDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertNotNull(result.createdAt());
        assertEquals("test@email.com", result.email());
        assertEquals("Test Company", result.companyName());
        assertEquals("Technology", result.sector());
        assertEquals("https://test.com", result.website());
        assertTrue(advertiserRepository.existsById(result.id()));
    }

    @Test
    void createAdvertiser_shouldReturnStatusPending() {

        // Arrange
        CreateAdvertiserRequestDTO requestDTO = buildRequest("test@email.com", "Test Company");

        // Act
        AdvertiserResponseDTO result = advertiserService.createAdvertiser(requestDTO);

        // Assert
        assertEquals(ProfileStatus.PENDING, result.status());
    }

    @Test
    void createAdvertiser_shouldThrowEmailAlreadyExistsException_whenEmailExists() {

        // Arrange
        saveAdvertiser("test@email.com", "Test Company", ProfileStatus.PENDING);
        CreateAdvertiserRequestDTO requestDTO = buildRequest("test@email.com", "Other Company");

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class,
                () -> advertiserService.createAdvertiser(requestDTO));
    }

    @Test
    void getAllAdvertisers_shouldReturnAllAdvertisers() {

        // Arrange
        saveAdvertiser("first@email.com", "First Company", ProfileStatus.PENDING);
        saveAdvertiser("second@email.com", "Second Company", ProfileStatus.ACTIVE);

        // Act
        List<AdvertiserResponseDTO> result = advertiserService.getAllAdvertisers();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.email().equals("first@email.com")));
        assertTrue(result.stream().anyMatch(a -> a.email().equals("second@email.com")));
    }

    @Test
    void getAllAdvertisers_shouldReturnEmptyList_whenNoAdvertisers() {

        // Act
        List<AdvertiserResponseDTO> result = advertiserService.getAllAdvertisers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAdvertiserById_shouldReturnAdvertiser_whenExists() {

        // Arrange
        Advertiser saved = saveAdvertiser("test@email.com", "Test Company", ProfileStatus.PENDING);

        // Act
        AdvertiserResponseDTO result = advertiserService.getAdvertiserById(saved.getId());

        // Assert
        assertEquals(saved.getId(), result.id());
        assertEquals("test@email.com", result.email());
        assertEquals("Test Company", result.companyName());
    }

    @Test
    void getAdvertiserById_shouldThrowProfileNotExistException_whenNotFound() {

        // Act & Assert
        assertThrows(ProfileNotExistException.class,
                () -> advertiserService.getAdvertiserById(999L));
    }

    @Test
    void advertiserExists_shouldReturnTrue_whenExists() {

        // Arrange
        Advertiser saved = saveAdvertiser("test@email.com", "Test Company", ProfileStatus.PENDING);

        // Act
        boolean result = advertiserService.advertiserExists(saved.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void advertiserExists_shouldReturnFalse_whenNotExists() {

        // Act
        boolean result = advertiserService.advertiserExists(999L);

        // Assert
        assertFalse(result);
    }
}
