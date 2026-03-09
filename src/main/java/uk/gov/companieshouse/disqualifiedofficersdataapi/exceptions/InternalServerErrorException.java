package uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String msg) {
        super(msg);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
