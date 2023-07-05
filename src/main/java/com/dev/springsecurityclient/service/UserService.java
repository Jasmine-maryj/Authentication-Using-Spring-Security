package com.dev.springsecurityclient.service;

import com.dev.springsecurityclient.entity.User;
import com.dev.springsecurityclient.entity.VerificationToken;
import com.dev.springsecurityclient.model.PasswordDTO;
import com.dev.springsecurityclient.model.UserDTO;

import java.util.Optional;

public interface UserService {
    User registerUser(UserDTO userDTO);

    void saveVerificationToken(String token, User user);

    String tokenVerification(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetToken(User user, String token);

    String validatePasswordReset(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfUserPasswordPresent(User user, String oldPassword);
}
