package com.dev.springsecurityclient.controller;

import com.dev.springsecurityclient.entity.VerificationToken;
import com.dev.springsecurityclient.event.RegistrationProcess;
import com.dev.springsecurityclient.entity.User;
import com.dev.springsecurityclient.model.PasswordDTO;
import com.dev.springsecurityclient.model.UserDTO;
import com.dev.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class UserRegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserDTO userDTO, final HttpServletRequest httpServletRequest){
        User user = userService.registerUser(userDTO);
        applicationEventPublisher.publishEvent(new RegistrationProcess(user, applicationUrl(httpServletRequest)));
        return "Successfully registered!";
    }

    private String applicationUrl(HttpServletRequest httpServletRequest) {
        return "http://"
                +httpServletRequest.getServerName()
                +":"
                +httpServletRequest.getServerPort()
                +httpServletRequest.getContextPath();
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){
        String result = userService.tokenVerification(token);
        if(result.equalsIgnoreCase("valid")){
            return "Your account is verified!";
        }
        return "Bad User Request";
    }

    @GetMapping("/resendVerificationLink")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request){
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenToMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    private void resendVerificationTokenToMail(User user, String applicationUrl, VerificationToken token) {
        String url = applicationUrl + "/verifyRegistration?token=" + token.getToken();
        log.info("Click the link to verify the registration " + url);
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordDTO passwordDTO, HttpServletRequest request){
        User user = userService.findUserByEmail(passwordDTO.getEmail());

        String passwordResetUrl = "";

        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetToken(user, token);
            passwordResetUrl = PasswordResetTokenUrl(user, applicationUrl(request), token);
        }
        return passwordResetUrl;
    }

    private String PasswordResetTokenUrl(User user, String applicationUrl, String token) {
        String passwordRestUrl = applicationUrl + "/resetPassword?token=" + token;
        log.info("Click the link to reset the password " + passwordRestUrl);
        return "Reset password link sent!";
    }

    @PostMapping("/saveNewPassword")
    public String saveNewPassword(@RequestParam("token") String token, @RequestBody PasswordDTO passwordDTO){
        String result = userService.validatePasswordReset(token);
        if(!result.equalsIgnoreCase("valid")){
            return "Invalid Token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.changePassword(user.get(), passwordDTO.getNewPassword());
            return "Password Reset Successfully";
        }else{
            return "Invalid token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordDTO passwordDTO){
        User user = userService.findUserByEmail(passwordDTO.getEmail());
        if(!userService.checkIfUserPasswordPresent(user, passwordDTO.getOldPassword())){
            return "Invalid password";
        }
        userService.changePassword(user, passwordDTO.getNewPassword());
        return "Password Changed Successfully!";
    }
}
