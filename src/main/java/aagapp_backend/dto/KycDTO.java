package aagapp_backend.dto;

import aagapp_backend.entity.kyc.KycEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KycDTO {
    private Long id;
    private Long userOrVendorId;
    private String role;
    private String mobileNumber;
    private String mailId;
    private String aadharNo;
    private String panNo;
    private String aadharImage;
    private String panImage;

    private String kycStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Kolkata")
    private Date createdAt;


    public KycDTO(KycEntity entity, String kycStatus) {
        this.id = entity.getId();
        this.userOrVendorId = entity.getUserOrVendorId();
        this.role = entity.getRole();
        this.mobileNumber = entity.getMobileNumber();
        this.mailId = entity.getMailId();
        this.aadharNo = entity.getAadharNo();
        this.panNo = entity.getPanNo();
        this.aadharImage = entity.getAadharImage();
        this.panImage = entity.getPanImage();
        this.kycStatus = kycStatus;
        this.createdAt = entity.getCreatedAt();

    }

}

