package com.gs.bustrack.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author Carlos Juarez
 */
@Builder
@Getter @Setter
public class ApiResponse {
    
    private Boolean success;
    
    private String message;
}
