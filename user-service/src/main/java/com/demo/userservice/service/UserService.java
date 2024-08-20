package com.demo.userservice.service;


import com.demo.userservice.core.EncryptionUtils;
import com.demo.userservice.core.jwt.JwtUtil;
import com.demo.userservice.dto.RegisterRequestDto;
import com.demo.userservice.model.Address;
import com.demo.userservice.model.User;
import com.demo.userservice.model.UserRoleEnum;
import com.demo.userservice.model.VerificationToken;
import com.demo.userservice.repository.AddressRepository;
import com.demo.userservice.repository.UserRepository;
import com.demo.userservice.repository.VerificationTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AddressRepository addressRepository;
    private final JwtUtil jwtUtil;
//    private final CartRepository cartRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, JwtUtil jwtUtil,
                       AddressRepository addressRepository,
                       VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.addressRepository = addressRepository;
        this.jwtUtil = jwtUtil;
//        this.cartRepository = cartRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    // 관리자 인증 토큰
    private final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    // 이메일 인증 코드 전송
    public void sendVerificationCode(String email) {
        String encryptedEmail;
        try {
            encryptedEmail = EncryptionUtils.encrypt(email);
        } catch (Exception e) {
            throw new RuntimeException("이메일 암호화 실패", e);
        }

        // 중복 이메일 확인
        Optional<User> checkEmail = userRepository.findByEmail(encryptedEmail);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 6자리 인증 코드 생성
        String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        // 인증 코드 유효시간 설정 (예: 10분)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMinutes(10);

        // 기존 인증 토큰이 있는지 확인
        VerificationToken existingToken = verificationTokenRepository.findByEmail(encryptedEmail).orElse(null);

        if (existingToken != null) {
            // 기존 인증 토큰이 있고, 인증되지 않은 경우 새로운 인증 코드로 업데이트
            if (!existingToken.isVerified()) {
                existingToken.setVerificationCode(verificationCode);
                existingToken.setExpiryDate(expiryDate);
                verificationTokenRepository.save(existingToken);
            } else {
                throw new IllegalArgumentException("이미 인증된 이메일입니다.");
            }
        } else {
            // 새 인증 토큰 생성
            VerificationToken token = new VerificationToken(encryptedEmail, verificationCode, expiryDate);
            verificationTokenRepository.save(token);
        }

        // 이메일 전송
        String subject = "Your Verification Code";
        String text = "Your verification code is: " + verificationCode;
        sendEmail(email, subject, text);
    }

    // 이메일 인증 처리
    public String verifyEmail(String email, String verificationCode) {
        String encryptedEmail;
        try {
            encryptedEmail = EncryptionUtils.encrypt(email);
        } catch (Exception e) {
            throw new RuntimeException("이메일 암호화 실패", e);
        }

        VerificationToken token = verificationTokenRepository.findByEmail(encryptedEmail)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 요청입니다."));

        if (token.isVerified()) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다.");
        }

        if (!token.getVerificationCode().equals(verificationCode)) {
            throw new IllegalArgumentException("잘못된 인증 코드입니다.");
        }

        if (LocalDateTime.now().isAfter(token.getExpiryDate())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        token.setVerified(true);
        verificationTokenRepository.save(token);

        return "이메일 인증이 완료되었습니다.";
    }

    // 회원가입 처리 후 인증 토큰 삭제
    @Transactional // 트랜잭션 처리 추가
    public void join(@Valid RegisterRequestDto requestDto) {
        String email = requestDto.getEmail();
        String encryptedEmail;
        try {
            encryptedEmail = EncryptionUtils.encrypt(email);
        } catch (Exception e) {
            throw new RuntimeException("이메일 암호화 실패", e);
        }

        VerificationToken token = verificationTokenRepository.findByEmail(encryptedEmail)
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증을 먼저 진행해 주세요."));
        if (!token.isVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 중복 아이디 확인
        Optional<User> checkUsername = userRepository.findByUsername(username);
        if (checkUsername.isPresent()) {
            throw new IllegalArgumentException("중복된 아이디가 존재합니다.");
        }

        // 이메일 중복 확인
        Optional<User> checkEmail = userRepository.findByEmail(encryptedEmail);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 이메일이 존재합니다.");
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
        String encryptedPhone;
        String encryptedName;
        String encryptedAddress;
        String encryptedAddressDetail;
        String encryptedZipcode;

        try {
            encryptedPhone = EncryptionUtils.encrypt(phone);
            encryptedName = EncryptionUtils.encrypt(name);
            encryptedAddress = EncryptionUtils.encrypt(requestDto.getAddress());
            encryptedAddressDetail = EncryptionUtils.encrypt(requestDto.getAddressDetail());
            encryptedZipcode = EncryptionUtils.encrypt(requestDto.getZipcode());
        } catch (Exception e) {
            throw new RuntimeException("데이터 암호화 실패", e);
        }

        // 사용자 등록
        User user = new User(username, password, encryptedEmail, encryptedPhone, encryptedName, role);
        userRepository.save(user);

        // todo : cart 연결 필요
      /*  // 장바구니 생성
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);*/

        // Address 등록
        Address address = new Address(encryptedAddress, encryptedAddressDetail, encryptedZipcode,
                requestDto.isDefault(), requestDto.getAddressMessage(), user);
        addressRepository.save(address);

        // 인증 토큰 삭제
        verificationTokenRepository.deleteByEmail(encryptedEmail);
    }


    // 패스워드 변경
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
            // JWT 쿠키 삭제 (로그아웃 처리)
            jwtUtil.removeJwtCookie(response);
            System.out.println("패스워드 변경시 쿠키 삭제할거임" + response);
        } else {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
    }


    // 이메일 전송 메서드
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}

