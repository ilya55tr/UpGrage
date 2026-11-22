package by.ilyatr.msaccountreservation.exception;

public class ClientSummaryTimeoutException extends RuntimeException{
  public ClientSummaryTimeoutException() {
    super("Client summary timed out");
  }
}
