package ma.youcode.affiliatelink.features.profile.service;

import ma.youcode.affiliatelink.features.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {
    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }
}
