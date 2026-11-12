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
}
