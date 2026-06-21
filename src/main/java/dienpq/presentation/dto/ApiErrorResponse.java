package dienpq.presentation.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ApiErrorResponse {
        private int status;
        private String error;
        private Object message;
        private LocalDateTime timestamp;

        public ApiErrorResponse(int status, String error, Object message) {
                this.status = status;
                this.error = error;
                this.message = message;
                this.timestamp = LocalDateTime.now();
        }
}