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

    @Test
    void updateOffer_shouldUpdateFieldsAndTags_whenOfferExists() {

        // Arrange
        Offer offer = new Offer(1L, "Old Offer", "Old description", "https://old.com", Niche.TECH);
        offer.replaceTags(Set.of(new Tag("old")));

        UpdateOfferRequestDTO requestDTO = new UpdateOfferRequestDTO(
                "New Offer",
                "New description",
                "https://new.com",
                Niche.GAMING,
                Set.of("New")
        );

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("New Offer")
                .description("New description")
                .landingUrl("https://new.com")
                .niche(Niche.GAMING)
                .createdAt(LocalDateTime.now())
                .tags(Set.of("new"))
                .build();

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(tagRepository.findByNameIn(Set.of("new"))).thenReturn(List.of());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

        // Act
        OfferResponseDTO result = offerService.updateOffer(1L, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(responseDTO, result);

        assertEquals("New Offer", offer.getTitle());
        assertEquals("New description", offer.getDescription());
        assertEquals("https://new.com", offer.getLandingUrl());
        assertEquals(Niche.GAMING, offer.getNiche());
        assertEquals(Set.of("new"), tagNames(offer));

        // Verify
        verify(offerRepository).findById(1L);
        verify(tagRepository).findByNameIn(Set.of("new"));
        verify(tagRepository).save(argThat(tag -> tag.getName().equals("new")));
        verify(offerRepository).save(offer);
        verify(offerMapper).toResponseDTO(offer);
    }

    @Test
    void updateOffer_shouldThrow_whenOfferNotFound() {

        // Arrange
        UpdateOfferRequestDTO requestDTO = new UpdateOfferRequestDTO(
                "New Offer",
                "New description",
                "https://new.com",
                Niche.GAMING,
                Set.of("New")
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        OfferNotFoundException exception = assertThrows(
                OfferNotFoundException.class,
                () -> offerService.updateOffer(1L, requestDTO)
        );

        // Assert
        assertNotNull(exception);
        assertEquals("Offer not found with id: 1", exception.getMessage());

        // Verify
        verify(offerRepository).findById(1L);
        verify(offerRepository, never()).save(any(Offer.class));
        verifyNoInteractions(tagRepository, offerMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchOffers_shouldApplyAllFilters_whenCriteriaAreProvided() {

        // Arrange
        OfferSearchCriteria criteria = new OfferSearchCriteria(Niche.TECH, 1L, "java", "spring");
        Pageable pageable = PageRequest.of(0, 10);

        Offer offer = new Offer(1L, "Spring Offer", "Test description", "https://test.com", Niche.TECH);

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .id(1L)
                .advertiserId(1L)
                .title("Spring Offer")
                .niche(Niche.TECH)
                .tags(Set.of("java"))
                .build();

        try (MockedStatic<OfferSpecifications> specifications = mockStatic(OfferSpecifications.class)) {
            specifications.when(() -> OfferSpecifications.hasNiche(Niche.TECH)).thenReturn(mock(Specification.class));
            specifications.when(() -> OfferSpecifications.hasAdvertiser(1L)).thenReturn(mock(Specification.class));
            specifications.when(() -> OfferSpecifications.hasTag("java")).thenReturn(mock(Specification.class));
            specifications.when(() -> OfferSpecifications.titleContains("spring")).thenReturn(mock(Specification.class));

            when(offerRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(offer), pageable, 1));
            when(offerMapper.toResponseDTO(offer)).thenReturn(responseDTO);

            // Act
            Page<OfferResponseDTO> result = offerService.searchOffers(criteria, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(List.of(responseDTO), result.getContent());

            // Verify
            specifications.verify(() -> OfferSpecifications.hasNiche(Niche.TECH));
            specifications.verify(() -> OfferSpecifications.hasAdvertiser(1L));
            specifications.verify(() -> OfferSpecifications.hasTag("java"));
            specifications.verify(() -> OfferSpecifications.titleContains("spring"));
            verify(offerRepository).findAll(any(Specification.class), eq(pageable));
            verify(offerMapper).toResponseDTO(offer);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchOffers_shouldIgnoreBlankTagAndKeyword() {

        // Arrange
        OfferSearchCriteria criteria = new OfferSearchCriteria(Niche.TECH, null, "   ", "");
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<OfferSpecifications> specifications = mockStatic(OfferSpecifications.class)) {
            specifications.when(() -> OfferSpecifications.hasNiche(Niche.TECH)).thenReturn(mock(Specification.class));

            when(offerRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty(pageable));

            // Act
            Page<OfferResponseDTO> result = offerService.searchOffers(criteria, pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());

            // Verify
            specifications.verify(() -> OfferSpecifications.hasNiche(Niche.TECH));
            specifications.verify(() -> OfferSpecifications.hasAdvertiser(anyLong()), never());
            specifications.verify(() -> OfferSpecifications.hasTag(anyString()), never());
            specifications.verify(() -> OfferSpecifications.titleContains(anyString()), never());
            verify(offerRepository).findAll(any(Specification.class), eq(pageable));
            verifyNoInteractions(offerMapper);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchOffers_shouldReturnAll_whenCriteriaAreEmpty() {

        // Arrange
        OfferSearchCriteria criteria = new OfferSearchCriteria(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Offer offer1 = new Offer(1L, "Offer 1", "Description 1", "https://test1.com", Niche.TECH);
        Offer offer2 = new Offer(2L, "Offer 2", "Description 2", "https://test2.com", Niche.GAMING);

        OfferResponseDTO responseDTO1 = OfferResponseDTO.builder().id(1L).title("Offer 1").build();
        OfferResponseDTO responseDTO2 = OfferResponseDTO.builder().id(2L).title("Offer 2").build();

        try (MockedStatic<OfferSpecifications> specifications = mockStatic(OfferSpecifications.class)) {
            when(offerRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(offer1, offer2), pageable, 2));
            when(offerMapper.toResponseDTO(offer1)).thenReturn(responseDTO1);
            when(offerMapper.toResponseDTO(offer2)).thenReturn(responseDTO2);

            // Act
            Page<OfferResponseDTO> result = offerService.searchOffers(criteria, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(List.of(responseDTO1, responseDTO2), result.getContent());

            // Verify
            specifications.verifyNoInteractions();
            verify(offerRepository).findAll(any(Specification.class), eq(pageable));
            verify(offerMapper).toResponseDTO(offer1);
            verify(offerMapper).toResponseDTO(offer2);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    void updateOffer_shouldClearTags_whenTagsAreNullOrEmpty(Set<String> tags) {

        // Arrange
        Offer offer = new Offer(1L, "Old Offer", "Old description", "https://old.com", Niche.TECH);
        offer.replaceTags(Set.of(new Tag("old")));

        UpdateOfferRequestDTO requestDTO = new UpdateOfferRequestDTO(
                "New Offer",
                "New description",
                "https://new.com",
                Niche.GAMING,
                tags
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(offer)).thenReturn(offer);

        // Act
        offerService.updateOffer(1L, requestDTO);

        // Assert
        assertTrue(offer.getTags().isEmpty());

        // Verify
        verify(offerRepository).findById(1L);
        verify(offerRepository).save(offer);
        verifyNoInteractions(tagRepository);
    }

    @Test
    void updateOffer_shouldReuseExistingTagsAndCreateMissingOnes() {

        // Arrange
        Offer offer = new Offer(1L, "Old Offer", "Old description", "https://old.com", Niche.TECH);
        offer.replaceTags(Set.of(new Tag("old")));

        UpdateOfferRequestDTO requestDTO = new UpdateOfferRequestDTO(
                "New Offer",
                "New description",
                "https://new.com",
                Niche.GAMING,
                Set.of(" Java ", "Spring")
        );

        Tag existingTag = new Tag("java");

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(tagRepository.findByNameIn(Set.of("java", "spring"))).thenReturn(List.of(existingTag));
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(offerRepository.save(offer)).thenReturn(offer);

        // Act
        offerService.updateOffer(1L, requestDTO);

        // Assert
        assertEquals(Set.of("java", "spring"), tagNames(offer));
        assertTrue(offer.getTags().stream().anyMatch(tag -> tag == existingTag));

        // Verify
        verify(tagRepository).findByNameIn(Set.of("java", "spring"));
        verify(tagRepository, times(1)).save(any(Tag.class));
        verify(tagRepository).save(argThat(tag -> tag.getName().equals("spring")));
        verify(offerRepository).save(offer);
    }

    @Test
    void createOffer_shouldNormalizeTagNames_beforeLookup() {

        // Arrange
        CreateOfferRequestDTO requestDTO = new CreateOfferRequestDTO(
                1L,
                "Test Offer",
                "Test description",
                "https://test.com",
                Niche.TECH,
                Set.of("  Java ", "JAVA", "Spring Boot")
        );

        when(profileApi.advertiserExists(1L)).thenReturn(true);
        when(profileApi.isActiveAdvertiser(1L)).thenReturn(true);
        when(tagRepository.findByNameIn(Set.of("java", "spring boot")))
                .thenReturn(List.of(new Tag("java"), new Tag("spring boot")));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        offerService.createOffer(requestDTO);

        // Assert
        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(offerCaptor.capture());

        assertEquals(Set.of("java", "spring boot"), tagNames(offerCaptor.getValue()));

        // Verify
        verify(tagRepository).findByNameIn(Set.of("java", "spring boot"));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchOffers_shouldApplyOnlyNicheFilter_whenOnlyNicheIsProvided() {

        // Arrange
        OfferSearchCriteria criteria = new OfferSearchCriteria(Niche.TECH, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<OfferSpecifications> specifications = mockStatic(OfferSpecifications.class)) {
            specifications.when(() -> OfferSpecifications.hasNiche(Niche.TECH)).thenReturn(mock(Specification.class));

            when(offerRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty(pageable));

            // Act
            Page<OfferResponseDTO> result = offerService.searchOffers(criteria, pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());

            // Verify
            specifications.verify(() -> OfferSpecifications.hasNiche(Niche.TECH));
            specifications.verify(() -> OfferSpecifications.hasAdvertiser(anyLong()), never());
            specifications.verify(() -> OfferSpecifications.hasTag(anyString()), never());
            specifications.verify(() -> OfferSpecifications.titleContains(anyString()), never());
            verify(offerRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchOffers_shouldApplyOnlyAdvertiserFilter_whenOnlyAdvertiserIdIsProvided() {

        // Arrange
        OfferSearchCriteria criteria = new OfferSearchCriteria(null, 1L, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<OfferSpecifications> specifications = mockStatic(OfferSpecifications.class)) {
            specifications.when(() -> OfferSpecifications.hasAdvertiser(1L)).thenReturn(mock(Specification.class));

            when(offerRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty(pageable));

            // Act
            Page<OfferResponseDTO> result = offerService.searchOffers(criteria, pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());

            // Verify
            specifications.verify(() -> OfferSpecifications.hasAdvertiser(1L));
            specifications.verify(() -> OfferSpecifications.hasNiche(any(Niche.class)), never());
            specifications.verify(() -> OfferSpecifications.hasTag(anyString()), never());
            specifications.verify(() -> OfferSpecifications.titleContains(anyString()), never());
            verify(offerRepository).findAll(any(Specification.class), eq(pageable));
        }
    }
}
