package com.taingy.expensetracker.service;

public interface OtpService {

    String generateOtp(String email);
    boolean verifyOtp(String email, String otpCode);
}
