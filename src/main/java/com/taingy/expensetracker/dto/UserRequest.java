package com.taingy.expensetracker.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserRequest {

    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String password;
    private String role;

}
