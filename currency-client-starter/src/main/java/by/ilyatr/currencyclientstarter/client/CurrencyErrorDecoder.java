package by.ilyatr.currencyclientstarter.client;

import by.ilyatr.currencyclientstarter.exception.CurrencyApiClientErrorException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiInvalidResponseException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiServerErrorException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CurrencyErrorDecoder implements ErrorDecoder {
  @Override
  public Exception decode(String s, Response response) {
    int status = response.status();
    String body = readBody(response);

    if(status >= 400 && status < 500){
      return new CurrencyApiClientErrorException(status, body);
    }
    if (status >= 502 && status <=504){
      return new CurrencyApiServerErrorException(status, body);
    }
    return new CurrencyApiInvalidResponseException("Unexpected HTTP status " + status + " from Currency API");
  }

  private String readBody(Response response){
    if(response.body() == null){
      return "";
    }
    try {
      return Util.toString(response.body().asReader(StandardCharsets.UTF_8));
    }catch (IOException e){
      return "<unreadable body>";
    }
  }
}
