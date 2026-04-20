package com.bgaidos.booking.web;

import com.bgaidos.booking.exception.BaseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_PREFIX = "urn:problem-type:";

    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBase(BaseException ex) {
        return problem(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleInvalidCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled() {
        return problem(HttpStatus.BAD_REQUEST, "email not verified");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity() {
        return problem(HttpStatus.CONFLICT, "data integrity violation");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var errors = new LinkedHashMap<String, String>();
        for (var fieldError : ex.getBindingResult().getFieldErrors()) {
            var message = fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage();
            errors.putIfAbsent(fieldError.getField(), message);
        }
        var pd = problem(HttpStatus.BAD_REQUEST, "request validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setType(URI.create(TYPE_PREFIX + problemTypeSuffix(status)));
        return pd;
    }

    private static String problemTypeSuffix(HttpStatus status) {
        return status.name().toLowerCase().replace('_', '-');
    }
}
