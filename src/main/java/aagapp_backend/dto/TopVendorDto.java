package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopVendorDto {
    private Long vendorId;
    private String vendorName;
    private Long followerCount;


    public TopVendorDto(Long vendorId, String vendorName, Long followerCount) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.followerCount = followerCount;
    }

}