package com.dev.springsecurityclient.event.listener;

import com.dev.springsecurityclient.entity.User;
import com.dev.springsecurityclient.event.RegistrationProcess;
import com.dev.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationProcessListener implements ApplicationListener<RegistrationProcess> {

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationProcess event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationToken(token, user);

        String url = event.getRegistrationLink() + "/verifyRegistration?token=" + token;

        log.info("Click the link to verify the registration" + url);
    }
}
