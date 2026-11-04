package by.ilyatr.msaccountreservation.exception;


import by.ilyatr.currencyclientstarter.exception.CurrencyApiClientErrorException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiInvalidResponseException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiRateLimitExceededException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiServerErrorException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiUnavailableException;
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

  @ExceptionHandler(CurrencyApiClientErrorException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyClientError(CurrencyApiClientErrorException exception) {
    log.warn("Currency API rejected request: status={}, body={}",
        exception.getStatus(), exception.getResponseBody());
    return buildResponse(ErrorCode.CURRENCY_REQUEST_REJECTED,
        exception.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(CurrencyApiServerErrorException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyServerError(CurrencyApiServerErrorException exception) {
    log.error("Currency API failed: status={}, body={}",
        exception.getStatus(), exception.getResponseBody());
    return buildResponse(ErrorCode.CURRENCY_SERVICE_ERROR,
        "Currency service error", HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(CurrencyApiInvalidResponseException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyInvalidResponse(CurrencyApiInvalidResponseException exception) {
    log.error("Currency API invalid response: {}", exception.getMessage());
    return buildResponse(ErrorCode.CURRENCY_SERVICE_ERROR,
        "Currency service error", HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(CurrencyApiUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyUnavailable(CurrencyApiUnavailableException exception) {
    log.error("Currency API is unavailable", exception);
    return buildResponse(ErrorCode.CURRENCY_SERVICE_UNAVAILABLE,
        "Currency service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
  }

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

  @ExceptionHandler(CurrencyApiRateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyRateLimit(CurrencyApiRateLimitExceededException exception) {
    log.warn("Outgoing rate limit to Currency API exceeded");
    return buildResponse(ErrorCode.CURRENCY_RATE_LIMIT_EXCEEDED,
        "Too many currency requests, try later", HttpStatus.TOO_MANY_REQUESTS);
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
