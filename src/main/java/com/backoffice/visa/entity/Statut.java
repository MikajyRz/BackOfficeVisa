package com.backoffice.visa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "statut")
public class Statut {

    @Id
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(name = "ordre_affichage", nullable = false)
    private Integer ordreAffichage;

    public Statut() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Integer getOrdreAffichage() { return ordreAffichage; }
    public void setOrdreAffichage(Integer ordreAffichage) { this.ordreAffichage = ordreAffichage; }
}
