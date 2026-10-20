package ma.youcode.affiliatelink.features.profile.service;

import lombok.AllArgsConstructor;
import ma.youcode.affiliatelink.features.profile.repository.AdvertiserRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdvertiserService {
    private final AdvertiserRepository advertiserRepository;
}
