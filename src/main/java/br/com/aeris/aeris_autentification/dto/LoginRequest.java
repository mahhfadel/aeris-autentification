package br.com.aeris.aeris_autentification.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String senha;
}
