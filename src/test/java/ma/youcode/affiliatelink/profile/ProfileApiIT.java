package ma.youcode.affiliatelink.profile;

import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Testcontainers
@Transactional
public class ProfileApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ProfileApi profileApi;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    private Advertiser saveAdvertiser(ProfileStatus status) {
        return advertiserRepository.saveAndFlush(Advertiser.builder()
                .email("test@email.com")
                .companyName("Test Company")
                .sector("Technology")
                .website("https://test.com")
                .status(status)
                .build());
    }

    @Test
    void advertiserExists_shouldReturnTrue_whenAdvertiserInDatabase() {

        // Arrange
        Advertiser saved = saveAdvertiser(ProfileStatus.PENDING);

        // Act
        boolean result = profileApi.advertiserExists(saved.getId());

        // Assert
        assertTrue(result);
    }
}
