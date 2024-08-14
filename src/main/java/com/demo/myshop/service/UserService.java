package com.demo.myshop.service;


import com.demo.myshop.core.EncryptionUtils;
import com.demo.myshop.dto.RegisterRequestDto;
import com.demo.myshop.jwt.JwtUtilWithRedis;
import com.demo.myshop.model.Address;
import com.demo.myshop.model.Cart;
import com.demo.myshop.model.User;
import com.demo.myshop.model.UserRoleEnum;
import com.demo.myshop.repository.AddressRepository;
import com.demo.myshop.repository.CartRepository;
import com.demo.myshop.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AddressRepository addressRepository;
    private final JwtUtilWithRedis jwtUtilWithRedis;
    private final CartRepository cartRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, AddressRepository addressRepository, JwtUtilWithRedis jwtUtilWithRedis
    , CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.addressRepository = addressRepository;
        this.jwtUtilWithRedis = jwtUtilWithRedis;
        this.cartRepository = cartRepository;
    }
    // ADMIN_TOKEN
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    // 회원 가입
    public void join(RegisterRequestDto requestDto) {
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
        String name = requestDto.getName();

        // 사용자 ROLE 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 암호가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        // 데이터 암호화
        String encryptedEmail;
        String encryptedPhone;
        String encryptedName;
        String encryptedAddress;
        String encryptedAddressDetail;
        String encryptedZipcode;

        try {
            encryptedEmail = EncryptionUtils.encrypt(email);
            encryptedPhone = EncryptionUtils.encrypt(phone);
            encryptedName = EncryptionUtils.encrypt(name);
            encryptedAddress = EncryptionUtils.encrypt(requestDto.getAddress());
            encryptedAddressDetail = EncryptionUtils.encrypt(requestDto.getAddressDetail());
            encryptedZipcode = EncryptionUtils.encrypt(requestDto.getZipcode());
        } catch (Exception e) {
            throw new RuntimeException("데이터 암호화 실패", e);
        }

        // 사용자 등록
        String verificationToken = UUID.randomUUID().toString();
        User user = new User(username, password, encryptedEmail, encryptedPhone, encryptedName, role, false, verificationToken); // 토큰 생성 및 저장
        userRepository.save(user);

        // 장바구니 생성
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);
        System.out.println("user.getUsername() = " + user.getUsername());

        // 주소 등록
        Address address = new Address(encryptedAddress, encryptedAddressDetail, encryptedZipcode,
                requestDto.getName(), requestDto.isDefaultAddress(),
                requestDto.getAddressPhoneNumber(), requestDto.getAddressMessage(), user);
        // 주소 리포지토리에 저장
        addressRepository.save(address);

        // 이메일 발송
        String verificationLink = "http://localhost:8080/api/user/verify?email=" + encryptedEmail + "&token=" + verificationToken;
        String subject = "Email Verification";
        String text = "Please click the following link to verify your email: " + verificationLink;
        sendEmail(email, subject, text);
    }

    // 이메일 인증 체크
    public String verifyEmail(String encryptedEmail, String token) {
        Optional<User> userOptional = userRepository.findByEmail(encryptedEmail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
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

    public void changePassword(String username, String oldPassword, String newPassword, HttpServletResponse response) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 기존 비밀번호 확인
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("기존 비밀번호가 올바르지 않습니다.");
            }

            // 새로운 비밀번호 설정
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // 레디스 토큰 제거 -> 모든 기기에서 로그아웃 처리 ?! 는 아닌 듯 ?!
            jwtUtilWithRedis.invalidateUserTokens(username, response);
            System.out.println(username + "님 로그아웃 처리한다 !!");

        } else {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
    }

    // 이메일 전송
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElse(null); // 사용자가 없으면 null 반환
    }
}