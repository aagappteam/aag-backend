package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorDTO {
    private Long id;
    private String name;

    public VendorDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
