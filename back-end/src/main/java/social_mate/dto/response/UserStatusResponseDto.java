package social_mate.dto.response; // Hoặc package dto của bạn
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatusResponseDto {
    private Long userId;
    private String status; // "ONLINE" hoặc "OFFLINE"
}