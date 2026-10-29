package ma.youcode.affiliatelink.offer.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offer_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Getter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "tags")
    private Set<Offer> offers = new HashSet<>();

    public Tag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name is required");
        }
        this.name = normalize(name);
    }

    public static String normalize(String name) {
        return name.trim().toLowerCase();
    }
}
