package ma.youcode.affiliatelink.features.profile.modal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "profiles_advertisers")
@PrimaryKeyJoinColumn(name = "id")
public class Advertiser extends Profile {
    @Column(name = "company_name", nullable = false, unique = true)
    private String companyName;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String website;
}
