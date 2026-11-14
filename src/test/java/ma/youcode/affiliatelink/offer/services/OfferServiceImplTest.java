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
}
