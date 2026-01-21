package com.banking.system.transaction.domain.exception;

public class TransferAlreadyExistException extends RuntimeException {
  public TransferAlreadyExistException(String message) {
    super(message);
  }
}
