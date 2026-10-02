package by.ilyatr.msaccountreservation.exception;


import by.ilyatr.msaccountreservation.model.ErrorCode;
import by.ilyatr.msaccountreservation.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ClientNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleClientNotFound(ClientNotFoundException exception) {
    return buildResponse(
        ErrorCode.CLIENT_NOT_FOUND,
        exception.getMessage(),
        HttpStatus.NOT_FOUND
    );
  }

  @ExceptionHandler(ClientAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleClientAlreadyExists(ClientAlreadyExistsException exception) {
    return buildResponse(
        ErrorCode.CLIENT_ALREADY_EXISTS,
        exception.getMessage(),
        HttpStatus.CONFLICT
    );
  }

  @ExceptionHandler(ClientHasActiveAccountsException.class)
  public ResponseEntity<ErrorResponse> handleClientHasActiveAccounts(ClientHasActiveAccountsException exception) {
    return buildResponse(
        ErrorCode.CLIENT_HAS_ACTIVE_ACCOUNTS,
        exception.getMessage(),
        HttpStatus.CONFLICT
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("Validation failed");
    return buildResponse(
        ErrorCode.VALIDATION_ERROR,
        message,
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler({
      ConstraintViolationException.class,
      MethodArgumentTypeMismatchException.class,
      MissingServletRequestParameterException.class,
      HttpMessageNotReadableException.class,
      IllegalArgumentException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
    return buildResponse(
        ErrorCode.VALIDATION_ERROR,
        exception.getMessage() != null
            ? exception.getMessage()
            : "Invalid request",
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalError(Exception exception) {
    log.error("Unexpected error", exception);
    return buildResponse(
        ErrorCode.INTERNAL_ERROR,
        "Internal server error",
        HttpStatus.INTERNAL_SERVER_ERROR
    );
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      ErrorCode errorCode,
      String description,
      HttpStatus status) {
    ErrorResponse response = ErrorResponse.builder()
        .errorCode(errorCode)
        .errorDescription(description)
        .statusCode(status.value())
        .build();
    return ResponseEntity
        .status(status)
        .body(response);
  }
}
