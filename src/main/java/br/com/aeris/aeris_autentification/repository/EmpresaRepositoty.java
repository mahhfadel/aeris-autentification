package br.com.aeris.aeris_autentification.repository;

import br.com.aeris.aeris_autentification.model.Empresa;
import br.com.aeris.aeris_autentification.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepositoty extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByNome(String nome);
}
