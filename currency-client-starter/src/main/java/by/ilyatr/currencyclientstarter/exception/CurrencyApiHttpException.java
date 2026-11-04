package by.ilyatr.currencyclientstarter.exception;

import lombok.Getter;

@Getter
public abstract class CurrencyApiHttpException extends CurrencyApiException {
  private final Integer status;
  private final String responseBody;

  protected CurrencyApiHttpException(String message, Integer status, String responseBody){
    super(message);
    this.status = status;
    this.responseBody = responseBody;
  }
}
