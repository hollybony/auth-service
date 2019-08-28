package com.gs.bustrack.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Carlos Juarez
 */
@Getter
@Setter
public class SignUpRequest {

    @NotNull
    @Size(min = 3, max = 15)
    private String username;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 8, max = 20)
    private String password;
    
    @Size(min = 8, max = 8)
    private String serviceId;
}
