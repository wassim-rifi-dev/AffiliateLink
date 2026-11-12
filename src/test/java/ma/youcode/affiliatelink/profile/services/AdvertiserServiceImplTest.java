package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.profile.dto.request.CreateAdvertiserRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.AdvertiserResponseDTO;
import ma.youcode.affiliatelink.profile.mapper.AdvertiserMapper;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.AdvertiserRepository;
import ma.youcode.affiliatelink.profile.service.impl.AdvertiserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvertiserServiceImplTest {

    @Mock
    private AdvertiserRepository advertiserRepository;

    @Mock
    private AdvertiserMapper advertiserMapper;

    @InjectMocks
    private AdvertiserServiceImpl advertiserService;

    @Test
    void createAdvertiser_shouldReturnResponseDTO_whenEmailDoesNotExist() {

        // Arrange
        CreateAdvertiserRequestDTO requestDTO = new CreateAdvertiserRequestDTO(
                "test@email.com",
                "test",
                "Technology",
                "https://test.com"
        );

        Advertiser advertiser = new Advertiser();

        AdvertiserResponseDTO responseDTO = AdvertiserResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .status(ProfileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .companyName("Test Company")
                .sector("Technology")
                .website("https://test.com")
                .build();

        when(advertiserRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(advertiserMapper.toEntity(requestDTO)).thenReturn(advertiser);
        when(advertiserRepository.save(advertiser)).thenReturn(advertiser);
        when(advertiserMapper.toResponseDTO(advertiser)).thenReturn(responseDTO);

        // Act
        AdvertiserResponseDTO result = advertiserService.createAdvertiser(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO , result);

        // Verify
        verify(advertiserRepository).existsByEmail(requestDTO.email());
        verify(advertiserMapper).toEntity(requestDTO);
        verify(advertiserRepository).save(advertiser);
        verify(advertiserMapper).toResponseDTO(advertiser);
    }

    @Test
    void createAdvertiser_shouldThrowEmailAlreadyExistsException_whenEmailExists() {

        // Arrange
        CreateAdvertiserRequestDTO requestDTO = new CreateAdvertiserRequestDTO(
                "test@email.com",
                "test",
                "Technology",
                "https://test.com"
        );

        Advertiser advertiser = new Advertiser();

        when(advertiserRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        // Act
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> advertiserService.createAdvertiser(requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Email already existe.", exception.getMessage());

        // Verify
        verify(advertiserRepository).existsByEmail(requestDTO.email());
        verify(advertiserMapper, never()).toEntity(requestDTO);
        verify(advertiserRepository, never()).save(advertiser);
        verify(advertiserMapper, never()).toResponseDTO(advertiser);
    }
}
