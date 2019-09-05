package com.gs.bustrack.auth.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author Carlos Juarez
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SigninException extends RuntimeException{
    
    public SigninException(String message) {
        super(message);
    }
    
    public SigninException(String message, Object... args) {
        super(String.format(message, args));
    }
}
