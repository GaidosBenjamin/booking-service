package com.bgaidos.booking.web;

import com.bgaidos.booking.exception.BaseException;
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

import java.net.URI;
import java.util.LinkedHashMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_PREFIX = "urn:problem-type:";

    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBase(BaseException ex) {
        log.error("base exception ({}): {}", ex.getStatus(), ex.getMessage(), ex);
        return problem(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(BadCredentialsException ex) {
        log.error("bad credentials: {}", ex.getMessage(), ex);
        return problem(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled(DisabledException ex) {
        log.error("account disabled: {}", ex.getMessage(), ex);
        return problem(HttpStatus.BAD_REQUEST, "email not verified");
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ProblemDetail handleAccessDenied(RuntimeException ex) {
        log.error("access denied: {}", ex.getMessage(), ex);
        return problem(HttpStatus.FORBIDDEN, "insufficient permissions");
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ProblemDetail handleOAuth2Authentication(OAuth2AuthenticationException ex) {
        log.error("oauth2 authentication failed: {}", ex.getMessage(), ex);
        return problem(HttpStatus.UNAUTHORIZED, "invalid bearer token");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("data integrity violation: {}", ex.getMessage(), ex);
        return problem(HttpStatus.CONFLICT, "data integrity violation");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var errors = new LinkedHashMap<String, String>();
        for (var fieldError : ex.getBindingResult().getFieldErrors()) {
            var message = fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage();
            errors.putIfAbsent(fieldError.getField(), message);
        }
        log.error("request validation failed: {}", errors);
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
