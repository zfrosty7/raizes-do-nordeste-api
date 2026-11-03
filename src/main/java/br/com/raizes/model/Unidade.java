package br.com.raizes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Tabela UNIDADE (uma loja da rede). */
@Entity
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cidade;

    protected Unidade() {
    }

    public Unidade(String nome, String cidade) {
        this.nome = nome;
        this.cidade = cidade;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
}
