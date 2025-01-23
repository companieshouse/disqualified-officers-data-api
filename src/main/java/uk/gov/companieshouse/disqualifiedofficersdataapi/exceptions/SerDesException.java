package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class SerDesException extends RuntimeException {

    public SerDesException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
