package com.smartcomplaints.complaint.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String role; // "CITIZEN" or "ADMIN"
}