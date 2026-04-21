package com.backoffice.visa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "type_demande")
public class TypeDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String libelle;

    public TypeDemande() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
