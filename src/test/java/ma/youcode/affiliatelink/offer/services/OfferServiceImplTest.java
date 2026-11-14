package ma.youcode.affiliatelink.offer.services;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.common.exception.OfferNotFoundException;
import ma.youcode.affiliatelink.common.exception.ProfileNotExistException;
import ma.youcode.affiliatelink.offer.dto.request.CreateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.request.OfferSearchCriteria;
import ma.youcode.affiliatelink.offer.dto.request.UpdateOfferRequestDTO;
import ma.youcode.affiliatelink.offer.dto.response.OfferResponseDTO;
import ma.youcode.affiliatelink.offer.mapper.OfferMapper;
import ma.youcode.affiliatelink.offer.model.entity.Offer;
import ma.youcode.affiliatelink.offer.model.entity.Tag;
import ma.youcode.affiliatelink.offer.repository.OfferRepository;
import ma.youcode.affiliatelink.offer.repository.OfferSpecifications;
import ma.youcode.affiliatelink.offer.repository.TagRepository;
import ma.youcode.affiliatelink.offer.service.impl.OfferServiceImpl;
import ma.youcode.affiliatelink.profile.ProfileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OfferServiceImplTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private OfferMapper offerMapper;

    @Mock
    private ProfileApi profileApi;

    @InjectMocks
    private OfferServiceImpl offerService;

    private static Set<String> tagNames(Offer offer) {
        return offer.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    @Test
    void createOffer_shouldSaveAndReturnDto_whenAdvertiserIsActive() {

        // Arrange
        CreateOfferRequestDTO requestDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                null
        );

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("Test Offer")
                .description("Test description")
                .landingUrl("https://test.com")
                .niche(Niche.TECH)
                .createdAt(LocalDateTime.now())
                .tags(Set.of())
                .build();

        when(profileApi.advertiserExists(1L)).thenReturn(true);
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(true);
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(offerMapper.toResponseDTO(any(Offer.class))).thenReturn(responseDTO);

        // Act
        OfferResponseDTO result = offerService.createOffer(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());
        Offer savedOffer = offerCaptor.getValue();

        assertEquals(1L, savedOffer.getAdvertiserId());
        assertEquals("Test Offer", savedOffer.getTitle());
        assertEquals("Test description", savedOffer.getDescription());
        assertEquals("https://test.com", savedOffer.getLandingUrl());
        assertEquals(Niche.TECH, savedOffer.getNiche());

        // Verify
        verify(profileApi).advertiserExists(1L);
        verify(profileApi).isActiveAdvertiser(1L);
        verify(offerMapper).toResponseDTO(savedOffer);
    }

    @Test
    void createOffer_shouldThrow_whenAdvertiserDoesNotExist() {

        // Arrange
        CreateOfferRequestDTO requestDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                null
        );

        when(profileApi.advertiserExists(1L)).thenReturn(false);

        // Act
        ProfileNotExistException exception = assertThrows(
                ProfileNotExistException.class,
                () -> offerService.createOffer(requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Advertiser not found with id: 1", exception.getMessage());

        // Verify
        verify(profileApi).advertiserExists(1L);
        verify(profileApi, never()).isActiveAdvertiser(anyLong());
        verify(offerRepository, never()).save(any(Offer.class));
        verifyNoInteractions(tagRepository, offerMapper);
    }

    @Test
    void createOffer_shouldThrow_whenAdvertiserIsNotActive() {

        // Arrange
        CreateOfferRequestDTO requestDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                null
        );

        when(profileApi.advertiserExists(1L)).thenReturn(true);
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(false);

        // Act
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> offerService.createOffer(requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Advertiser must be active to create an offer", exception.getMessage());

        // Verify
        verify(profileApi).advertiserExists(1L);
        verify(profileApi).isActiveAdvertiser(1L);
        verify(offerRepository, never()).save(any(Offer.class));
        verifyNoInteractions(tagRepository, offerMapper);
    }

    @Test
    void createOffer_shouldReuseExistingTagsAndCreateMissingOnes() {

        // Arrange
        CreateOfferRequestDTO requestDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                Set.of(" Java ", "Spring")
        );

        Tag existingTag = new Tag("java");

        when(profileApi.advertiserExists(1L)).thenReturn(true);
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(true);
        when(tagRepository.findByNameIn(Set.of("java", "spring"))).thenReturn(List.of(existingTag));
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        offerService.createOffer(requestDTO);

        // Assert
        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());
        Offer savedOffer = offerCaptor.getValue();

        assertEquals(Set.of("java", "spring"), tagNames(savedOffer));
        assertTrue(savedOffer.getTags().stream().anyMatch(tag -> tag == existingTag));

        // Verify
        verify(tagRepository).findByNameIn(Set.of("java", "spring"));
        verify(tagRepository, times(1)).save(any(Tag.class));
        verify(tagRepository).save(argThat(tag -> tag.getName().equals("spring")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createOffer_shouldHaveNoTags_whenTagsAreNullOrEmpty(Set<String> tags) {

        // Arrange
        CreateOfferRequestDTO requestDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                tags
        );

        when(profileApi.advertiserExists(1L)).thenReturn(true);
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(true);
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        offerService.createOffer(requestDTO);

        // Assert
        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());

        assertTrue(offerCaptor.getValue().getTags().isEmpty());

        // Verify
        verifyNoInteractions(tagRepository);
    }

    @Test
    void getOfferById_shouldReturnDto_whenOfferExists() {

        // Arrange
        Offer offer = new Offer(1L, "Test Offer", "Test description", "https://test.com", Niche.TECH);

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("Test Offer")
                .description("Test description")
                .landingUrl("https://test.com")
                .niche(Niche.TECH)
                .createdAt(LocalDateTime.now())
                .tags(Set.of())
                .build();

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // Act
        OfferResponseDTO result = offerService.getOfferById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        // Verify
        verify(offerRepository).findById(1L);
        verify(offerMapper).toResponseDTO(offer);
    }

    @Test
    void getOfferById_shouldThrow_whenOfferNotFound() {

        // Arrange
        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        OfferNotFoundException exception = assertThrows(
                OfferNotFoundException.class,
                () -> offerService.getOfferById(1L)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Offer not found with id: 1", exception.getMessage());

        // Verify
        verify(offerRepository).findById(1L);
        verifyNoInteractions(offerMapper);
    }
}
