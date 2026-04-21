package com.backoffice.visa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nationalite")
public class Nationalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String libelle;

    public Nationalite() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
