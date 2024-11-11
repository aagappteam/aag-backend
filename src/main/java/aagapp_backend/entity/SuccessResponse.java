package aagapp_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SuccessResponse<T> {
    private String message;
    private T data;
    private HttpStatus status;
    private int status_code;
}
