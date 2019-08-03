package com.gs.bustrack.auth.ex;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 *
 * @author Carlos Juarez
 */
@RestControllerAdvice
public class AuthExeceptionHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(AuthExeceptionHandler.class);

    @ExceptionHandler(value = {SignupConflictException.class})
    public ResponseEntity signupFailed(Exception ex, WebRequest req) {
        LOG.error("signup failed :(... ", ex);
        Map<String, String> errorMsg = new HashMap<>();
        errorMsg.put("code", "conflict");
        errorMsg.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}