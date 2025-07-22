package aagapp_backend.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KwickPayResponse {
    private String status;
    private String statuscode;
    private String message;
    private String txnid;
    private String utr;
    // Add other fields as per response
}
