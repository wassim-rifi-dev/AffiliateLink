package ma.youcode.affiliatelink.features.profile.modal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.youcode.affiliatelink.features.profile.modal.enums.Niche;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "profiles_publishers")
@PrimaryKeyJoinColumn(name = "id")
public class Publisher extends Profile {
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Niche niche;

    @Column(nullable = false)
    private String bio;

    @Column(nullable = false)
    private String country;

    public String getUserFullName() {
        return firstName + " " + lastName;
    }
}
