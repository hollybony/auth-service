package com.gs.bustrack.auth.services;

import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 *
 * @author Carlos Juarez
 */
@Component
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationListener.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        LOG.debug("Confirm email {} from app url {}", user.getEmail(), event.getAppUrl());
        VerificationToken token = userService.createVerificationToken(user, UUID.randomUUID().toString());
        String recipientAddress = user.getEmail();
        String subject = "BusTrack email confirmation";
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(String.format("The token is : [%s]. Click the following link to verify you email account: %s",
                token.getToken(), event.getAppUrl() + "?token=" + token.getToken()));
        mailSender.send(email);
    }
}
