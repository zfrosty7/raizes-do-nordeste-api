package br.com.raizes.config;

import br.com.raizes.model.Perfil;
import br.com.raizes.model.Produto;
import br.com.raizes.model.Unidade;
import br.com.raizes.model.Usuario;
import br.com.raizes.repository.ProdutoRepository;
import br.com.raizes.repository.UnidadeRepository;
import br.com.raizes.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * SEED: roda sempre que a API inicia. Se o banco estiver vazio, cadastra unidades, produtos e usuários de teste.
 * Se já tiver dados, não faz nada.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public DadosIniciais(UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository,
                         UsuarioRepository usuarioRepository) {
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        if (unidadeRepository.count() > 0) {
            return; // banco já populado
        }

        // Unidade 1 (Recife): produtos de id 1 a 5
        Long recife = unidadeRepository.save(new Unidade("Raízes do Nordeste - Boa Viagem", "Recife")).getId();
        produtoRepository.save(new Produto("Tapioca de queijo coalho", new BigDecimal("12.50"), 50, recife));
        produtoRepository.save(new Produto("Cuscuz recheado", new BigDecimal("15.00"), 40, recife));
        produtoRepository.save(new Produto("Bolo de macaxeira", new BigDecimal("9.00"), 30, recife));
        produtoRepository.save(new Produto("Suco de cajá", new BigDecimal("8.00"), 60, recife));
        produtoRepository.save(new Produto("Café da manhã completo", new BigDecimal("22.00"), 20, recife));

        // Unidade 2 (Fortaleza): produtos de id 6 a 9. O bolo (id 8) tem só 2 unidades, para testar "estoque insuficiente".
        Long fortaleza = unidadeRepository.save(new Unidade("Raízes do Nordeste - Meireles", "Fortaleza")).getId();
        produtoRepository.save(new Produto("Tapioca de queijo coalho", new BigDecimal("12.50"), 30, fortaleza));
        produtoRepository.save(new Produto("Cuscuz recheado", new BigDecimal("15.00"), 25, fortaleza));
        produtoRepository.save(new Produto("Bolo de macaxeira", new BigDecimal("9.00"), 2, fortaleza));
        produtoRepository.save(new Produto("Suco de cajá", new BigDecimal("8.00"), 40, fortaleza));

        // Usuários de teste (senha de todos: Senha@123). Só o hash é gravado.
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuarioRepository.save(new Usuario("Maria Cliente", "cliente@raizes.com", encoder.encode("Senha@123"), Perfil.CLIENTE, true));
        usuarioRepository.save(new Usuario("Ana Gerente", "gerente@raizes.com", encoder.encode("Senha@123"), Perfil.GERENTE, true));
    }
}
