package com.gs.bustrack.auth.services;

import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import java.util.UUID;
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

    @Autowired
    private UserService service;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        VerificationToken token = service.createVerificationToken(user, UUID.randomUUID().toString());
        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(String.format("The token is : %s %s." + token.getToken(), event.getAppUrl()));
        mailSender.send(email);
    }
}
