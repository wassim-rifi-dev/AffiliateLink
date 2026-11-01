package ma.youcode.affiliatelink.offer.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.youcode.affiliatelink.offer.service.CampaignService;
import ma.youcode.affiliatelink.profile.AdvertiserSuspended;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class AdvertiserSuspendedListener {

    private final CampaignService campaignService;

    @ApplicationModuleListener
    public void on(AdvertiserSuspended event) {
        log.info("AdvertiserSuspended event received: advertiserId={}", event.advertiserId());

        campaignService.pauseActiveCampaignsOfAdvertiser(event.advertiserId());
    }
}
