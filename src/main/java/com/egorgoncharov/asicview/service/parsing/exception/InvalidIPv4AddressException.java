package com.egorgoncharov.asicview.service.parsing.exception;

public class InvalidIPv4AddressException extends Exception {
    public InvalidIPv4AddressException(String errorMessage) {
        super(errorMessage);
    }
}
