package br.com.aeris.aeris_autentification.dto;

import br.com.aeris.aeris_autentification.model.PesquisaColaborador;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginColaboradorResponse {
    private String token;
    private String nome;
    private String email;
    private Long id_pesquisa;
    private String mensagem;
}
