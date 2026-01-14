package com.taingy.expensetracker.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String newPassword;
}
