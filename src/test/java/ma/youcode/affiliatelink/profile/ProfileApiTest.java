package ma.youcode.affiliatelink.profile;

import ma.youcode.affiliatelink.profile.service.AdvertiserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileApiTest {

    @Mock
    private AdvertiserService advertiserService;

    @InjectMocks
    private ProfileApi profileApi;

    @Test
    void advertiserExists_shouldReturnTrue_whenServiceReturnsTrue() {

        // Arrange
        when(advertiserService.advertiserExists(1L)).thenReturn(true);

        // Act
        boolean result = profileApi.advertiserExists(1L);

        // Assert
        assertTrue(result);

        // Verify
        verify(advertiserService).advertiserExists(1L);
    }

    @Test
    void advertiserExists_shouldReturnFalse_whenServiceReturnsFalse() {

        // Arrange
        when(advertiserService.advertiserExists(1L)).thenReturn(false);

        // Act
        boolean result = profileApi.advertiserExists(1L);

        // Assert
        assertFalse(result);

        // Verify
        verify(advertiserService).advertiserExists(1L);
    }
}
