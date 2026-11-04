package by.ilyatr.currencyclientstarter.exception;


public class CurrencyApiUnavailableException extends CurrencyApiException {

  public CurrencyApiUnavailableException(Throwable cause) {
    super("Currency API is unavailable", cause);
  }
}
