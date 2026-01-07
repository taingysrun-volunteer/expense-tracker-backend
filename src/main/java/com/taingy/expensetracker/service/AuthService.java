package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.AuthResponse;
import com.taingy.expensetracker.dto.LoginRequest;
import com.taingy.expensetracker.dto.ResponseMessage;
import com.taingy.expensetracker.dto.UserRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);
    ResponseMessage register(UserRequest request);

}
