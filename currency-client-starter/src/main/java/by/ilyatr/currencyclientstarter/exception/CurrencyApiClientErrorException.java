package by.ilyatr.currencyclientstarter.exception;

public class CurrencyApiClientErrorException extends CurrencyApiHttpException {
  public CurrencyApiClientErrorException(int status, String body) {
    super("Currency API rejected request: HTTP " + status, status, body);
  }
}
