package ma.youcode.affiliatelink.profile.entity;

import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileEntityTest {

    @Test
    void newProfile_shouldBePendingByDefault() {

        // Act
        Publisher publisher = new Publisher();

        // Assert
        assertEquals(ProfileStatus.PENDING, publisher.getStatus());
    }

    @Test
    void setStatus_shouldActivatePendingProfile() {

        // Arrange
        Publisher publisher = new Publisher();

        // Act
        publisher.setStatus(ProfileStatus.ACTIVE);

        // Assert
        assertEquals(ProfileStatus.ACTIVE, publisher.getStatus());
    }
}
