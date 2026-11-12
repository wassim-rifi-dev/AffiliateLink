package ma.youcode.affiliatelink.offer.model.entity;

import ma.youcode.affiliatelink.common.enums.Niche;
import ma.youcode.affiliatelink.common.exception.BusinessRuleException;
import ma.youcode.affiliatelink.offer.model.enums.CampaignStatus;
import ma.youcode.affiliatelink.offer.model.enums.PayoutModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OfferTest {

    @Test
    void constructor_shouldInitializeFields() {

        // Act
        Offer offer = new Offer(1L, "Summer Sale", "Big discounts", "https://shop.com", Niche.TECH);

        // Assert
        assertEquals(1L, offer.getAdvertiserId());
        assertEquals("Summer Sale", offer.getTitle());
        assertEquals("Big discounts", offer.getDescription());
        assertEquals("https://shop.com", offer.getLandingUrl());
        assertEquals(Niche.TECH, offer.getNiche());
        assertNull(offer.getCreatedAt());
        assertTrue(offer.getCampaigns().isEmpty());
        assertTrue(offer.getTags().isEmpty());
    }

    @Test
    void onCreate_shouldSetCreatedAt() {

        // Arrange
        Offer offer = newOffer();
        assertNull(offer.getCreatedAt());

        // Act
        offer.onCreate();

        // Assert
        assertNotNull(offer.getCreatedAt());
    }

    @Test
    void update_shouldReplaceAllFields() {

        // Arrange
        Offer offer = newOffer();

        // Act
        offer.update("Winter Sale", "New description", "https://new-shop.com", Niche.GAMING);

        // Assert
        assertEquals("Winter Sale", offer.getTitle());
        assertEquals("New description", offer.getDescription());
        assertEquals("https://new-shop.com", offer.getLandingUrl());
        assertEquals(Niche.GAMING, offer.getNiche());
        assertEquals(1L, offer.getAdvertiserId());
    }

    @Test
    void replaceTags_shouldClearAndAddNewTags() {

        // Arrange
        Offer offer = newOffer();
        offer.replaceTags(Set.of(new Tag("old")));

        // Act
        offer.replaceTags(Set.of(new Tag("java"), new Tag("spring")));

        // Assert
        assertEquals(Set.of(new Tag("java"), new Tag("spring")), offer.getTags());
    }

    private static Offer newOffer() {
        return new Offer(1L, "Summer Sale", "Big discounts", "https://shop.com", Niche.TECH);
    }
}
