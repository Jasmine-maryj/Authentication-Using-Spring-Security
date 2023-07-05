package com.dev.springsecurityclient.service;

import com.dev.springsecurityclient.entity.PasswordResetToken;
import com.dev.springsecurityclient.entity.User;
import com.dev.springsecurityclient.entity.VerificationToken;
import com.dev.springsecurityclient.model.PasswordDTO;
import com.dev.springsecurityclient.model.UserDTO;
import com.dev.springsecurityclient.repository.PasswordRepository;
import com.dev.springsecurityclient.repository.UserRepository;
import com.dev.springsecurityclient.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(("USER"));
        return userRepository.save(user);
    }

    @Override
    public void saveVerificationToken(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(user, token);
        verificationRepository.save(verificationToken);
    }

    @Override
    public String tokenVerification(String token) {
        VerificationToken verificationToken = verificationRepository.findByToken(token);
        if(verificationToken == null){
            return "Invalid Token";
        }
        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if(verificationToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0){
            verificationRepository.delete(verificationToken);
            return "Expired!";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "Valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetToken(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token);
        passwordRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordReset(String token) {
        PasswordResetToken passwordResetToken = passwordRepository.findByToken(token);
        if(passwordResetToken == null){
            return "Invalid Token";
        }
        User user = passwordResetToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if(passwordResetToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0){
            passwordRepository.delete(passwordResetToken);
            return "Expired!";
        }
        return "Valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfUserPasswordPresent(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
