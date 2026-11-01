package ma.youcode.affiliatelink.offer.listener;

import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.offer.service.CampaignService;
import ma.youcode.affiliatelink.profile.AdvertiserSuspended;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AdvertiserSuspendedListener {

    private final CampaignService campaignService;

    @ApplicationModuleListener
    public void on(AdvertiserSuspended event) {
        campaignService.pauseActiveCampaignsOfAdvertiser(event.advertiserId());
    }
}
