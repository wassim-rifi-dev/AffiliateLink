package ma.youcode.affiliatelink.features.profile.controller;

import ma.youcode.affiliatelink.features.profile.service.PublisherService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublisherController {
    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }
}
