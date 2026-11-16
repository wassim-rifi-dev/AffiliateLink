package ma.youcode.affiliatelink.profile.enums;

import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileStatusTest {

    @ParameterizedTest(name = "{0} -> {1} : {2}")
    @CsvSource({
            "PENDING,   PENDING,   false",
            "PENDING,   ACTIVE,    true",
            "PENDING,   SUSPENDED, false",
            "ACTIVE,    PENDING,   false",
            "ACTIVE,    ACTIVE,    false",
            "ACTIVE,    SUSPENDED, true",
            "SUSPENDED, PENDING,   false",
            "SUSPENDED, ACTIVE,    true",
            "SUSPENDED, SUSPENDED, false"
    })
    void changeStatus_shouldRespectAllowedTransitions(ProfileStatus current, ProfileStatus target, boolean expected) {

        // Act
        boolean result = current.changeStatus(target);

        // Assert
        assertEquals(expected, result);
    }
}
