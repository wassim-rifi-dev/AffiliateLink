package ma.youcode.affiliatelink.features.profile.controller;

import lombok.RequiredArgsConstructor;
import ma.youcode.affiliatelink.features.profile.service.AdvertiserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advertisers")
@RequiredArgsConstructor
public class AdvertiserController {
    private final AdvertiserService advertiserService;
}
