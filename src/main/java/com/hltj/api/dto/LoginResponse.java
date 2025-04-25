package com.hltj.api.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private UserDTO hluser;
}