package by.ilyatr.currencyclientstarter.exception;

public class CurrencyApiServerErrorException extends CurrencyApiHttpException {
  public CurrencyApiServerErrorException(int status, String body) {
    super("Currency API failed: HTTP " + status, status, body);
  }
}