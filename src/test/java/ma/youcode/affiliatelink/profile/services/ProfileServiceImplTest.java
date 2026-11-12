package ma.youcode.affiliatelink.profile.services;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.InvalidStatusTransitionException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.profile.AdvertiserSuspended;
import ma.youcode.affiliatelink.profile.dto.request.UpdateProfileStatusRequestDTO;
import ma.youcode.affiliatelink.profile.dto.response.ProfileStatusResponseDTO;
import ma.youcode.affiliatelink.profile.mapper.ProfileMapper;
import ma.youcode.affiliatelink.profile.model.entity.Advertiser;
import ma.youcode.affiliatelink.profile.model.entity.Publisher;
import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import ma.youcode.affiliatelink.profile.repository.ProfileRepository;
import ma.youcode.affiliatelink.profile.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {
    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void updateStatus_shouldActivatePendingProfile() {

        // Arrange
        Long id = 1L;

        Publisher publisher = Publisher.builder()
                .id(id)
                .email("test@email.com")
                .firstName("John")
                .lastName("Doe")
                .niche(Niche.TECH)
                .bio("Tech content creator")
                .country("Morocco")
                .status(ProfileStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        UpdateProfileStatusRequestDTO requestDTO = new UpdateProfileStatusRequestDTO(ProfileStatus.ACTIVE);

        ProfileStatusResponseDTO responseDTO = ProfileStatusResponseDTO.builder()
                .id(id)
                .email("test@email.com")
                .status(ProfileStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(profileRepository.findById(id)).thenReturn(Optional.of(publisher));
        when(profileMapper.toStatusResponseDTO(publisher)).thenReturn(responseDTO);

        // Act
        ProfileStatusResponseDTO result = profileService.updateStatus(id, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);
        assertEquals(ProfileStatus.ACTIVE, publisher.getStatus());

        // Verify
        verify(profileRepository).findById(id);
        verify(profileMapper).toStatusResponseDTO(publisher);
    }
}
