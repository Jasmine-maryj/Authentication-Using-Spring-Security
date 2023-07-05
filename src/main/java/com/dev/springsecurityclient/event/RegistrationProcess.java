package com.dev.springsecurityclient.event;

import com.dev.springsecurityclient.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
@Getter
@Setter
public class RegistrationProcess extends ApplicationEvent {

    private User user;
    private String registrationLink;

    public RegistrationProcess(User user, String registrationLink) {
        super(user);
        this.user = user;
        this.registrationLink = registrationLink;
    }
}
