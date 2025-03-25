package aagapp_backend.entity.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "top_vendor_cache", indexes = {
        @Index(name = "idx_service_provider_id", columnList = "serviceProviderId"),
        @Index(name = "idx_rank", columnList = "rank"),
        @Index(name = "idx_price", columnList = "price")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopVendorCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private Long serviceProviderId;
    private Double price;
    private Integer rank;
    private String profileImage;
    private String vendorName;
    private Timestamp updatedAt;


    public TopVendorCache(Long serviceProviderId, Double price, Integer rank, String profileImage, String vendorName) {
        this.serviceProviderId = serviceProviderId;
        this.price = price;
        this.rank = rank;
        this.profileImage = profileImage;
        this.vendorName = vendorName;
    }

}
