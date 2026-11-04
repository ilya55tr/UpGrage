package by.ilyatr.currencyclientstarter.exception;

public class CurrencyApiRateLimitExceededException extends CurrencyApiException {
  public CurrencyApiRateLimitExceededException(Throwable cause) {
    super("Outgoing rate limit to Currency API exceeded", cause);
  }
}
