package br.com.aeris.aeris_autentification.dto;

import br.com.aeris.aeris_autentification.model.PesquisaColaborador;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginColaboradorResponse {
    private String token;
    private String nome;
    private String email;
    private Long idPesquisa;
    private String mensagem;
}
