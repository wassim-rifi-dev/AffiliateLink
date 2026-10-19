package ma.youcode.affiliatelink.features.profile.modal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.youcode.affiliatelink.features.profile.modal.enums.ProfileStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private ProfileStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
