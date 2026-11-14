package ma.youcode.affiliatelink.offer.listener;

import ma.youcode.affiliatelink.offer.service.CampaignService;
import ma.youcode.affiliatelink.profile.AdvertiserSuspended;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class AdvertiserSuspendedListenerTest {

    @Mock
    private CampaignService campaignService;

    @InjectMocks
    private AdvertiserSuspendedListener advertiserSuspendedListener;

    @Test
    void on_shouldPauseActiveCampaignsOfAdvertiser() {

        // Arrange
        AdvertiserSuspended event = new AdvertiserSuspended(1L);

        // Act
        advertiserSuspendedListener.on(event);

        // Verify
        verify(campaignService).pauseActiveCampaignsOfAdvertiser(1L);
        verifyNoMoreInteractions(campaignService);
    }
}
