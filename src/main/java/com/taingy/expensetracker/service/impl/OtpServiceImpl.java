package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.model.EmailOtp;
import com.taingy.expensetracker.repository.EmailOtpRepository;
import com.taingy.expensetracker.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);

    @Value("${otp.expire-in-minute}")
    private int OTP_EXPIRY_MINUTES;
    private static final SecureRandom random = new SecureRandom();

    private final EmailOtpRepository emailOtpRepository;

    @Autowired
    public OtpServiceImpl(EmailOtpRepository emailOtpRepository) {
        this.emailOtpRepository = emailOtpRepository;
    }

    @Override
    public String generateOtp(String email) {
        List<EmailOtp> existingOtps = emailOtpRepository.findByEmailAndVerifiedFalse(email);
        if (!existingOtps.isEmpty()) {
            logger.info("Invalidating {} existing OTP(s) for email: {}", existingOtps.size(), email);
        }

        String otpCode = String.format("%06d", random.nextInt(1000000));

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtpCode(otpCode);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        emailOtp.setVerified(false);

        emailOtpRepository.save(emailOtp);
        logger.info("Generated OTP for email: {}", email);

        return otpCode;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<EmailOtp> otpOpt = emailOtpRepository.findByEmailAndOtpCodeAndVerifiedFalseAndExpiresAtAfter(
                email, otpCode, LocalDateTime.now());

        if (otpOpt.isEmpty()) {
            logger.warn("Invalid or expired OTP attempt for email: {}", email);
            return false;
        }

        EmailOtp emailOtp = otpOpt.get();
        emailOtp.setVerified(true);
        emailOtp.setVerifiedAt(LocalDateTime.now());
        emailOtpRepository.save(emailOtp);

        logger.info("OTP verified successfully for email: {}", email);
        return true;
    }

}
