package ma.youcode.affiliatelink.profile.entity;

import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
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

    @Test
    void setStatus_shouldSuspendThenReactivateProfile() {

        // Arrange
        Publisher publisher = new Publisher();
        publisher.setStatus(ProfileStatus.ACTIVE);

        // Act & Assert
        publisher.setStatus(ProfileStatus.SUSPENDED);
        assertEquals(ProfileStatus.SUSPENDED, publisher.getStatus());

        publisher.setStatus(ProfileStatus.ACTIVE);
        assertEquals(ProfileStatus.ACTIVE, publisher.getStatus());
    }

    @Test
    void setStatus_shouldThrow_whenTransitionIsInvalid() {

        // Arrange
        Publisher publisher = new Publisher();

        // Act
        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> publisher.setStatus(ProfileStatus.SUSPENDED)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Invalid status transition from PENDING to SUSPENDED", exception.getMessage());
        assertEquals(ProfileStatus.PENDING, publisher.getStatus());
    }

    @Test
    void setStatus_shouldAcceptAnyStatus_whenCurrentStatusIsNull() {

        // Arrange
        // With @SuperBuilder, status is null because @Builder.Default is missing
        Publisher publisher = Publisher.builder().build();
        assertNull(publisher.getStatus());

        // Act
        publisher.setStatus(ProfileStatus.SUSPENDED);

        // Assert
        assertEquals(ProfileStatus.SUSPENDED, publisher.getStatus());
    }
}
