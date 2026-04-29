package com.backoffice.visa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "piece_demande_specifique", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_demande", "id_type_piece"})
})
public class PieceDemandeSpecifique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_type_piece", nullable = false)
    private TypePieceSpecifique typePieceSpecifique;

    @Column(nullable = false)
    private Boolean presente = false;

    @Column(name = "fichier_path")
    private String fichierPath;

    public PieceDemandeSpecifique() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }
    public TypePieceSpecifique getTypePieceSpecifique() { return typePieceSpecifique; }
    public void setTypePieceSpecifique(TypePieceSpecifique typePieceSpecifique) { this.typePieceSpecifique = typePieceSpecifique; }
    public Boolean getPresente() { return presente; }
    public void setPresente(Boolean presente) { this.presente = presente; }
    public String getFichierPath() { return fichierPath; }
    public void setFichierPath(String fichierPath) { this.fichierPath = fichierPath; }
}
