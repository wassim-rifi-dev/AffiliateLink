package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.profile.mapper.PublisherMapper;
import ma.youcode.affiliatelink.profile.repository.PublisherRepository;
import ma.youcode.affiliatelink.profile.service.impl.PublisherServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private PublisherMapper publisherMapper;

    @InjectMocks
    private PublisherServiceImpl publisherService;
}
