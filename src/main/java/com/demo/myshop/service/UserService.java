package com.demo.myshop.service;


import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.model.User;
import com.demo.myshop.model.UserRoleEnum;
import com.demo.myshop.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    public void join(RegisterRequestDto requestDto) {  // TODO : 주소 부분 추가
        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원 중복 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        // email 중복확인
        String email = requestDto.getEmail();
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 Email 입니다.");
        }

        String phone = requestDto.getPhone();

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        // 사용자 등록
        User user = new User(username, password, email, phone, role, false, UUID.randomUUID().toString()); // 토큰 생성 및 저장
        userRepository.save(user);

        // 이메일 발송
        String verificationLink = "http://localhost:8080/api/user/verify?email=" + email + "&token=" + user.getEmailVerificationToken();
        String subject = "Email Verification";
        String text = "Please click the following link to verify your email: " + verificationLink;
        sendEmail(email, subject, text);
    }

    public String handleEmailVerification(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return "이미 가입된 유저입니다.";
        } else {
            // 이메일 발송 로직
            String verificationToken = UUID.randomUUID().toString(); // 인증 토큰 생성
            String verificationLink = "http://localhost:8080/api/user/verify?email=" + email + "&token=" + verificationToken;
            String subject = "Email Verification";
            String text = "Please click the following link to verify your email: " + verificationLink;
            sendEmail(email, subject, text);
            return "Verification email sent to " + email;
        }
    }

    public String verifyEmail(String email, String token) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 검증 로직: 제공된 토큰과 저장된 토큰이 일치하는지 확인
            if (token.equals(user.getEmailVerificationToken())) {
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null); // 인증 후 토큰 삭제
                userRepository.save(user);
                return "이메일 인증 성공. 이제 회원가입을 완료할 수 있습니다.";
            } else {
                return "유효하지 않은 인증 토큰입니다.";
            }
        } else {
            return "가입된 사용자가 없습니다.";
        }
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

}