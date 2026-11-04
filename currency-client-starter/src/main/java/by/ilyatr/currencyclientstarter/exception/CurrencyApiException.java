package by.ilyatr.currencyclientstarter.exception;

public abstract class CurrencyApiException extends RuntimeException {
  protected CurrencyApiException(String message) {
    super(message);
  }

  protected CurrencyApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
