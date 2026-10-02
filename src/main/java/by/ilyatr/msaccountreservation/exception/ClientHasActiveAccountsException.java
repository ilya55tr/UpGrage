package by.ilyatr.msaccountreservation.exception;

public class ClientHasActiveAccountsException extends RuntimeException {
  public ClientHasActiveAccountsException(String message) {
    super(message);
  }
}