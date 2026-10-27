package ma.youcode.affiliatelink.profile;

import ma.youcode.affiliatelink.profile.service.AdvertiserService;
import org.springframework.stereotype.Component;

@Component
public class ProfileApi {
    private final AdvertiserService advertiserService;

    public ProfileApi(AdvertiserService advertiserService) {
        this.advertiserService = advertiserService;
    }

    public boolean isActiveAdvertiser(Long id) {
        return advertiserService.isActiveAdvertiser(id);
    }
}
