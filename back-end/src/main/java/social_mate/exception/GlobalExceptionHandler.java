package social_mate.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(exception = NotMatchingOtpException.class)
	public ResponseEntity<?> handleNotMatchingOtpException(NotMatchingOtpException ex) {

		var response = responseMessage(400, "bad request", ex.getMessage());

		return ResponseEntity.status(400).body(response);

	}

	@ExceptionHandler(exception = ExpiredOtpException.class)

	public ResponseEntity<?> handleExpiredOtpException(ExpiredOtpException ex) {

		var response = responseMessage(400, "bad request", ex.getMessage());

		return ResponseEntity.status(400).body(response);

	}

	@ExceptionHandler(exception = EmailAlreadyExistsException.class)
	public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
		var response = responseMessage(400, "bad request", ex.getMessage());

		return ResponseEntity.status(400).body(response);
	}

	@ExceptionHandler(exception = NotMatchingRefreshTokenException.class)

	public ResponseEntity<?> handleNotMatchingRefreshTokenException(NotMatchingRefreshTokenException ex) {
		var response = responseMessage(400, "bad request", ex.getMessage());

		return ResponseEntity.status(400).body(response);
	}

	private Map<String, Object> responseMessage(int status, String error, String message) {

		Map<String, Object> response = new HashMap<>();

		response.put("status", status);
		response.put("error", error);
		response.put("message", message);

		return response;

	}

}
