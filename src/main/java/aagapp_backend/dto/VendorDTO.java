package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorDTO {
    private Long id;
    private String name;
    private String profilePicUrl;

    public VendorDTO(Long id, String name, String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
        this.id = id;
        this.name = name;
    }
}
