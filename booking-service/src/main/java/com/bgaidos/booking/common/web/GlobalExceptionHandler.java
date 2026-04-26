package com.bgaidos.booking.common.web;

import com.bgaidos.booking.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_PREFIX = "urn:problem-type:";

    // Expected: domain errors (4xx)
    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBase(BaseException ex) {
        log.debug("base exception ({}): {}", ex.getStatus(), ex.getMessage());
        return problem(ex.getStatus(), ex.getMessage());
    }

    // Expected: wrong password / unknown user
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(BadCredentialsException ex) {
        log.debug("bad credentials: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }

    // Expected: email not yet verified
    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled(DisabledException ex) {
        log.debug("account disabled: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "email not verified");
    }

    // Expected: insufficient permissions
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ProblemDetail handleAccessDenied(RuntimeException ex) {
        log.debug("access denied: {}", ex.getMessage());
        return problem(HttpStatus.FORBIDDEN, "insufficient permissions");
    }

    // Expected: missing or expired bearer token
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ProblemDetail handleOAuth2Authentication(OAuth2AuthenticationException ex) {
        log.debug("oauth2 authentication failed: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "invalid bearer token");
    }

    // Expected: wrong type in path/query param
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.debug("type mismatch for parameter '{}': {}", ex.getName(), ex.getValue());
        var detail = "invalid value for parameter '%s': %s".formatted(ex.getName(), ex.getValue());
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    // Expected: DTO validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var errors = new LinkedHashMap<String, String>();
        for (var fieldError : ex.getBindingResult().getFieldErrors()) {
            var message = fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage();
            errors.putIfAbsent(fieldError.getField(), message);
        }
        log.debug("validation failed: {}", errors);
        var pd = problem(HttpStatus.BAD_REQUEST, "request validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    // Unexpected: DB constraint violation — likely a bug
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("data integrity violation: {}", ex.getMessage(), ex);
        return problem(HttpStatus.CONFLICT, "data integrity violation");
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
