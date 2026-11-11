package ma.youcode.affiliatelink.profile.model.entity;

import ma.youcode.affiliatelink.profile.model.enums.ProfileStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    @Test
    @DisplayName("Un publisher créé a le statut PENDING")
    void shouldCreatePublisherWithPendingStatus() {
        Publisher publisher = new Publisher();

        assertEquals(ProfileStatus.PENDING, publisher.getStatus());
    }

    @Test
    @DisplayName("Un advertiser créé a le statut PENDING")
    void shouldCreateAdvertiserWithPendingStatus() {
        Advertiser advertiser = new Advertiser();

        assertEquals(ProfileStatus.PENDING, advertiser.getStatus());
    }
}