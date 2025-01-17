package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.InternalServerErrorException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.MethodNotAllowedException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerConfig {

    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";
    private static final String ATTRIBUTE = "javax.servlet.error.exception";

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    /**
     * Runtime exception handler. Acts as the catch-all scenario.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {InternalServerErrorException.class, Exception.class, SerDesException.class})
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        LOGGER.error(String.format("Unexpected exception, response code: %s",
                HttpStatus.INTERNAL_SERVER_ERROR), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, "Unable to process the request.");
        request.setAttribute(ATTRIBUTE, ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * NotFoundException handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        LOGGER.error(String.format("Resource not found, response code: %s",
                HttpStatus.NOT_FOUND), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, "Resource not found.");
        request.setAttribute(ATTRIBUTE, ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    /**
     * MethodNotAllowedException exception handler.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {MethodNotAllowedException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleMethodNotAllowedException(Exception ex,
            WebRequest request) {
        LOGGER.error(String.format("Unable to process the request, response code: %s",
                HttpStatus.METHOD_NOT_ALLOWED), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, "Unable to process the request.");
        request.setAttribute(ATTRIBUTE, ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * ServiceUnavailableException exception handler. To be thrown when there are connection issues.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {ServiceUnavailableException.class, DataAccessException.class})
    public ResponseEntity<Object> handleServiceUnavailableException(Exception ex,
            WebRequest request) {
        LOGGER.error(String.format("Service unavailable, response code: %s",
                HttpStatus.SERVICE_UNAVAILABLE), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, "Service unavailable.");
        request.setAttribute(ATTRIBUTE, ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * BadRequestException exception handler. Thrown when data is given in the wrong format.
     *
     * @param ex      exception to handle.
     * @param request request.
     * @return error response to return.
     */
    @ExceptionHandler(value = {BadRequestException.class, DateTimeParseException.class,
            HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        String msg = "Bad request";
        if (StringUtils.isBlank(request.getHeader("x-delta-at"))) {
            msg = "Bad request: no delta_at on header";
            LOGGER.error("%s, response code: %s".formatted(msg, HttpStatus.BAD_REQUEST), ex);
        }
        LOGGER.error("%s, response code: %s".formatted(msg, HttpStatus.BAD_REQUEST), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, msg);
        request.setAttribute(ATTRIBUTE, ex, 0);

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleConflictException(Exception ex, WebRequest request) {
        LOGGER.error(String.format("Conflict, response code: %s", HttpStatus.CONFLICT), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, "Conflict.");
        request.setAttribute(ATTRIBUTE, ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {BadGatewayException.class})
    public ResponseEntity<Object> handleBadGatewayException(Exception ex, WebRequest request) {
        LOGGER.error(String.format("BadGateway, response code: %s", HttpStatus.BAD_GATEWAY), ex);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put(TIMESTAMP, LocalDateTime.now());
        responseBody.put(MESSAGE, "BadGateway.");
        request.setAttribute(ATTRIBUTE, ex, 0);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_GATEWAY);
    }
}
