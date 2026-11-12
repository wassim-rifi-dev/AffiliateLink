package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
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
import java.util.List;
import java.util.Optional;

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

    @Test
    void getAllAdvertisers_shouldReturnMappedList() {

        // Arrange
        Advertiser advertiser1 = new Advertiser();
        Advertiser advertiser2 = new Advertiser();

        List<Advertiser> advertisers = List.of(advertiser1, advertiser2);

        AdvertiserResponseDTO responseDTO1 = AdvertiserResponseDTO.builder()
                .id(1L)
                .email("test1@email.com")
                .status(ProfileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .companyName("Test Company 1")
                .sector("Technology")
                .website("https://test1.com")
                .build();

        AdvertiserResponseDTO responseDTO2 = AdvertiserResponseDTO.builder()
                .id(2L)
                .email("test2@email.com")
                .status(ProfileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .companyName("Test Company 2")
                .sector("Finance")
                .website("https://test2.com")
                .build();

        when(advertiserRepository.findAll()).thenReturn(advertisers);
        when(advertiserMapper.toResponseDTO(advertiser1)).thenReturn(responseDTO1);
        when(advertiserMapper.toResponseDTO(advertiser2)).thenReturn(responseDTO2);

        // Act
        List<AdvertiserResponseDTO> result = advertiserService.getAllAdvertisers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of(responseDTO1, responseDTO2), result);

        // Verify
        verify(advertiserRepository).findAll();
        verify(advertiserMapper).toResponseDTO(advertiser1);
        verify(advertiserMapper).toResponseDTO(advertiser2);
    }

    @Test
    void getAllAdvertisers_shouldReturnEmptyList_whenNoAdvertisers() {

        // Arrange
        Advertiser advertiser = new Advertiser();

        when(advertiserRepository.findAll()).thenReturn(List.of());

        // Act
        List<AdvertiserResponseDTO> result = advertiserService.getAllAdvertisers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(advertiserRepository).findAll();
        verify(advertiserMapper, never()).toResponseDTO(advertiser);
    }

    @Test
    void getAdvertiserById_shouldReturnResponseDTO_whenAdvertiserExists() {

        // Arrange
        Long id = 1L;

        Advertiser advertiser = new Advertiser();

        AdvertiserResponseDTO responseDTO = AdvertiserResponseDTO.builder()
                .id(id)
                .email("test@email.com")
                .status(ProfileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .companyName("Test Company")
                .sector("Technology")
                .website("https://test.com")
                .build();

        when(advertiserRepository.findById(id)).thenReturn(Optional.of(advertiser));
        when(advertiserMapper.toResponseDTO(advertiser)).thenReturn(responseDTO);

        // Act
        AdvertiserResponseDTO result = advertiserService.getAdvertiserById(id);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        // Verify
        verify(advertiserRepository).findById(id);
        verify(advertiserMapper).toResponseDTO(advertiser);
    }

    @Test
    void getAdvertiserById_shouldThrowProfileNotExistException_whenAdvertiserNotFound() {

        // Arrange
        Long id = 1L;

        Advertiser advertiser = new Advertiser();

        when(advertiserRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ProfileNotExistException exception = assertThrows(
                ProfileNotExistException.class,
                () -> advertiserService.getAdvertiserById(id)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Advertiser not found with id: " + id, exception.getMessage());

        // Verify
        verify(advertiserRepository).findById(id);
        verify(advertiserMapper, never()).toResponseDTO(advertiser);
    }

    @Test
    void advertiserExists_shouldReturnTrue_whenRepositoryReturnsTrue() {
        // Arrange
        Long id = 1L;

        when(advertiserRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = advertiserService.advertiserExists(id);

        // Assert
        assertTrue(result);

        // Verify
        verify(advertiserRepository).existsById(id);
    }

    @Test
    void advertiserExists_shouldReturnFalse_whenRepositoryReturnsFalse() {
        // Arrange
        Long id = 1L;

        when(advertiserRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = advertiserService.advertiserExists(id);

        // Assert
        assertFalse(result);

        // Verify
        verify(advertiserRepository).existsById(id);
    }

    @Test
    void isActiveAdvertiser_shouldReturnTrue_whenAdvertiserIsActive() {
        // Arrange
        Long id = 1L;

        Advertiser advertiser = Advertiser.builder()
                .id(1L)
                .companyName("test")
                .sector("Technology")
                .website("https://test.com")
                .email("test@gmail.com")
                .status(ProfileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(advertiserRepository.findById(id)).thenReturn(Optional.of(advertiser));

        // Act
        boolean result = advertiserService.isActiveAdvertiser(id);

        // Assert
        assertTrue(result);

        // Verify
        verify(advertiserRepository).findById(id);
    }

    @Test
    void isActiveAdvertiser_shouldReturnFalse_whenAdvertiserIsPending() {
        // Arrange
        Long id = 1L;

        Advertiser advertiser = Advertiser.builder()
                .id(1L)
                .companyName("test")
                .sector("Technology")
                .website("https://test.com")
                .email("test@gmail.com")
                .status(ProfileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(advertiserRepository.findById(id)).thenReturn(Optional.of(advertiser));

        // Act
        boolean result = advertiserService.isActiveAdvertiser(id);

        // Assert
        assertFalse(result);

        // Verify
        verify(advertiserRepository).findById(id);
    }

    @Test
    void isActiveAdvertiser_shouldReturnFalse_whenAdvertiserIsSuspended() {
        // Arrange
        Long id = 1L;

        Advertiser advertiser = Advertiser.builder()
                .id(1L)
                .companyName("test")
                .sector("Technology")
                .website("https://test.com")
                .email("test@gmail.com")
                .status(ProfileStatus.SUSPENDED)
                .createdAt(LocalDateTime.now())
                .build();

        when(advertiserRepository.findById(id)).thenReturn(Optional.of(advertiser));

        // Act
        boolean result = advertiserService.isActiveAdvertiser(id);

        // Assert
        assertFalse(result);

        // Verify
        verify(advertiserRepository).findById(id);
    }

    @Test
    void isActiveAdvertiser_shouldReturnFalse_whenAdvertiserNotFound() {
        // Arrange
        Long id = 1L;

        when(advertiserRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = advertiserService.isActiveAdvertiser(id);

        // Assert
        assertFalse(result);

        // Verify
        verify(advertiserRepository).findById(id);
    }
}
