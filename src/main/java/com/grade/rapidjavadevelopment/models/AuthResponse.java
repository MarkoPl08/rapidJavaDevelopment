package com.grade.rapidjavadevelopment.models;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }
}