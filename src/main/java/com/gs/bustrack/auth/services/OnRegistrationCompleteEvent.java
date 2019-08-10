package com.gs.bustrack.auth.services;

import com.gs.bustrack.auth.domain.User;
import java.util.Locale;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 *
 * @author Carlos Juarez
 */
@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private final String appUrl;

    private final Locale locale;

    private final User user;

    public OnRegistrationCompleteEvent(User user, Locale locale, String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }

}
