package by.ilyatr.msaccountreservation.exception;

public class ClientSummaryOverloaded extends RuntimeException{
  public ClientSummaryOverloaded() {
      super("Client summary executor is busy");
  }
}
