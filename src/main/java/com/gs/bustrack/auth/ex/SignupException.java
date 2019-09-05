package com.gs.bustrack.auth.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Carlos Juarez
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class SignupException extends RuntimeException {

    public SignupException(String message) {
        super(message);
    }
    
    public SignupException(String message, Object... args) {
        super(String.format(message, args));
    }
}