package com.hltj.api.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String phone;

    private String password;
}