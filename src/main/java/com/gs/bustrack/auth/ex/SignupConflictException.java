package com.gs.bustrack.auth.ex;

/**
 *
 * @author Carlos Juarez
 */
public class SignupConflictException extends RuntimeException {

    public SignupConflictException(String message) {
        super(message);
    }
}