package com.taingy.expensetracker.service;

public interface EmailService {

    void sendOtpEmail(String email, String otpCode, String firstName);
}
