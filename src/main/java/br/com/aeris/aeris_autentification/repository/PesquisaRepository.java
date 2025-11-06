package br.com.aeris.aeris_autentification.repository;

import br.com.aeris.aeris_autentification.model.Pesquisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PesquisaRepository extends JpaRepository<Pesquisa, Long> {
    boolean existsById(Long id);

}
