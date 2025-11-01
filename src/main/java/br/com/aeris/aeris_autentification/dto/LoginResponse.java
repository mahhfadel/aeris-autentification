package br.com.aeris.aeris_autentification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String nome;
    private String email;
    private Long empresa;
    private String mensagem;
}
