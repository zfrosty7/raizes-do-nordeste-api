package br.com.raizes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/** Tabela USUARIO. A senha nunca é guardada pura: só o hash (BCrypt). */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    // LGPD: o cliente precisa aceitar o uso dos dados, e guardamos quando aceitou
    private boolean consentimentoLgpd;
    private LocalDateTime consentimentoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, Perfil perfil, boolean consentimentoLgpd) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.consentimentoLgpd = consentimentoLgpd;
        this.consentimentoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
}
