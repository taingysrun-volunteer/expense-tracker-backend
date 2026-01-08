package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.*;
import org.apache.coyote.BadRequestException;

public interface AuthService {

    AuthResponse login(LoginRequest request);
    ResponseMessage register(UserRequest request);
    AuthResponse verifyOtpCode(VerifyOtpRequest request) throws BadRequestException;
    ResponseMessage resendOtp(ResendOtpRequest request) throws BadRequestException;
}
