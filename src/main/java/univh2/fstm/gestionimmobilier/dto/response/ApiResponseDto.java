package univh2.fstm.gestionimmobilier.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String errorCode;
    private String errorDetails;
}