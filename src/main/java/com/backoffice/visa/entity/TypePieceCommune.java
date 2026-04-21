package com.backoffice.visa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "type_piece_commune")
public class TypePieceCommune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false)
    private Boolean obligatoire = true;

    public TypePieceCommune() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Boolean getObligatoire() { return obligatoire; }
    public void setObligatoire(Boolean obligatoire) { this.obligatoire = obligatoire; }
}
