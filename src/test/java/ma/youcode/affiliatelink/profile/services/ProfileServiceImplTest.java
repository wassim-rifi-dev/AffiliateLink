package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.profile.mapper.ProfileMapper;
import ma.youcode.affiliatelink.profile.repository.ProfileRepository;
import ma.youcode.affiliatelink.profile.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {
    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;
}
