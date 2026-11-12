package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.EmailAlreadyExistsException;
import ma.youcode.affiliatelink.profile.dto.request.CreatePublisherRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.PublisherResponseDTO;
import ma.youcode.affiliatelink.profile.mapper.PublisherMapper;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.PublisherRepository;
import ma.youcode.affiliatelink.profile.service.impl.PublisherServiceImpl;
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
public class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private PublisherMapper publisherMapper;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    @Test
    void createPublisher_shouldReturnResponseDTO_whenEmailDoesNotExist() {

        // Arrange
        CreatePublisherRequestDTO requestDTO = new CreatePublisherRequestDTO(
                "test@email.com",
                "John",
                "Doe",
                Niche.TECH,
                "Tech content creator",
                "Morocco"
        );

        Publisher publisher = new Publisher();

        PublisherResponseDTO responseDTO = PublisherResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech content creator")
                .country("Morocco")
                .status(ProfileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(publisherRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(publisherMapper.toEntity(requestDTO)).thenReturn(publisher);
        when(publisherRepository.save(publisher)).thenReturn(publisher);
        when(publisherMapper.toResponseDTO(publisher)).thenReturn(responseDTO);

        // Act
        PublisherResponseDTO result = publisherService.createPublisher(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        // Verify
        verify(publisherRepository).existsByEmail(requestDTO.email());
        verify(publisherMapper).toEntity(requestDTO);
        verify(publisherRepository).save(publisher);
        verify(publisherMapper).toResponseDTO(publisher);
    }

    @Test
    void createPublisher_shouldThrowEmailAlreadyExistsException_whenEmailExists() {

        // Arrange
        CreatePublisherRequestDTO requestDTO = new CreatePublisherRequestDTO(
                "test@email.com",
                "John",
                "Doe",
                Niche.TECH,
                "Tech content creator",
                "Morocco"
        );

        Publisher publisher = new Publisher();

        when(publisherRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        // Act
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> publisherService.createPublisher(requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Email already existe.", exception.getMessage());

        // Verify
        verify(publisherRepository).existsByEmail(requestDTO.email());
        verify(publisherMapper, never()).toEntity(requestDTO);
        verify(publisherRepository, never()).save(publisher);
        verify(publisherMapper, never()).toResponseDTO(publisher);
    }

    @Test
    void getAllPublisher_shouldReturnMappedList() {

        // Arrange
        Publisher publisher1 = new Publisher();
        Publisher publisher2 = new Publisher();

        List<Publisher> publishers = List.of(publisher1, publisher2);

        PublisherResponseDTO responseDTO1 = PublisherResponseDTO.builder()
                .id(1L)
                .email("test1@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech content creator")
                .country("Morocco")
                .status(ProfileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        PublisherResponseDTO responseDTO2 = PublisherResponseDTO.builder()
                .id(2L)
                .email("test2@email.com")
                .firstName("Jane")
                .lastName("Smith")
                .niche(Niche.GAMING)
                .bio("Gaming streamer")
                .country("France")
                .status(ProfileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(publisherRepository.findAll()).thenReturn(publishers);
        when(publisherMapper.toResponseDTO(publisher1)).thenReturn(responseDTO1);
        when(publisherMapper.toResponseDTO(publisher2)).thenReturn(responseDTO2);

        // Act
        List<PublisherResponseDTO> result = publisherService.getAllPublisher();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(List.of(responseDTO1, responseDTO2), result);

        // Verify
        verify(publisherRepository).findAll();
        verify(publisherMapper).toResponseDTO(publisher1);
        verify(publisherMapper).toResponseDTO(publisher2);
    }

    @Test
    void getAllPublisher_shouldReturnEmptyList_whenNoPublishers() {

        // Arrange
        Publisher publisher = new Publisher();

        when(publisherRepository.findAll()).thenReturn(List.of());

        // Act
        List<PublisherResponseDTO> result = publisherService.getAllPublisher();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(publisherRepository).findAll();
        verify(publisherMapper, never()).toResponseDTO(publisher);
    }

    @Test
    void getPublisherById_shouldReturnResponseDTO_whenPublisherExists() {

        // Arrange
        Long id = 1L;

        Publisher publisher = new Publisher();

        PublisherResponseDTO responseDTO = PublisherResponseDTO.builder()
                .id(id)
                .email("test@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech content creator")
                .country("Morocco")
                .status(ProfileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(publisherRepository.findById(id)).thenReturn(Optional.of(publisher));
        when(publisherMapper.toResponseDTO(publisher)).thenReturn(responseDTO);

        // Act
        PublisherResponseDTO result = publisherService.getPublisherById(id);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        // Verify
        verify(publisherRepository).findById(id);
        verify(publisherMapper).toResponseDTO(publisher);
    }
}
