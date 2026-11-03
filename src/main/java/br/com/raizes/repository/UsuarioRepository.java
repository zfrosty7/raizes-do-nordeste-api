package br.com.raizes.repository;

import br.com.raizes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** O Spring cria sozinho os métodos de salvar/buscar. Os de "findBy..." são montados pelo nome. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
