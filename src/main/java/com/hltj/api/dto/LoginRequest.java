package com.hltj.api.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String jwcode;

    private String phone;

    private String password;
}