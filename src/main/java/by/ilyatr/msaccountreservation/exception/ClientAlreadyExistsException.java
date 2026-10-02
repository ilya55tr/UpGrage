package by.ilyatr.msaccountreservation.exception;

public class ClientAlreadyExistsException extends RuntimeException {

  public ClientAlreadyExistsException(Long mdmCode) {
    super("Client with mdmCode already exists: " + mdmCode);
  }
}
