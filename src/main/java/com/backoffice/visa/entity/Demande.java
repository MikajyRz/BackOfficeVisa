package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable")
    private VisaTransformable visaTransformable;

    @Column(name = "date_demande", nullable = false)
    private LocalDate dateDemande;

    @Column(name = "id_statut", nullable = false)
    private Integer statut = 1;

    // Constantes de statut
    public static final int STATUT_INCOMPLET = 0;
    public static final int STATUT_CREATION = 1;
    public static final int STATUT_SCANNE = 2;
    public static final int STATUT_APPROUVE = 3;
    public static final int STATUT_DUPLICATA_DEMANDE = 10;
    public static final int STATUT_DUPLICATA_VALIDE = 11;
    public static final int STATUT_DUPLICATA_REJETE = 12;
    public static final int STATUT_DUPLICATA_EMIS = 13;

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;

    @ManyToOne
    @JoinColumn(name = "id_type_visa", nullable = false)
    private TypeVisa typeVisa;

    @ManyToOne
    @JoinColumn(name = "id_type_demande", nullable = false)
    private TypeDemande typeDemande;

    @Column(name = "date_traitement")
    private LocalDate dateTraitement;

    public Demande() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public VisaTransformable getVisaTransformable() { return visaTransformable; }
    public void setVisaTransformable(VisaTransformable visaTransformable) { this.visaTransformable = visaTransformable; }
    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }
    public Integer getStatut() { return statut; }
    public void setStatut(Integer statut) { this.statut = statut; }
    public Demandeur getDemandeur() { return demandeur; }
    public void setDemandeur(Demandeur demandeur) { this.demandeur = demandeur; }
    public TypeVisa getTypeVisa() { return typeVisa; }
    public void setTypeVisa(TypeVisa typeVisa) { this.typeVisa = typeVisa; }
    public TypeDemande getTypeDemande() { return typeDemande; }
    public void setTypeDemande(TypeDemande typeDemande) { this.typeDemande = typeDemande; }
    public LocalDate getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDate dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getStatutLibelle() {
        return switch (statut) {
            case STATUT_INCOMPLET -> "Dossier";
            case STATUT_CREATION -> "Dossier créé";
            case STATUT_SCANNE -> "Dossier scanné";
            case STATUT_APPROUVE -> "Dossier approuvé";
            case STATUT_DUPLICATA_DEMANDE -> "Duplicata demandé";
            case STATUT_DUPLICATA_VALIDE -> "Duplicata validé";
            case STATUT_DUPLICATA_REJETE -> "Duplicata rejeté";
            case STATUT_DUPLICATA_EMIS -> "Duplicata émis";
            default -> "Inconnu (" + statut + ")";
        };
    }

    public boolean estDossierApprouve() {
        return statut != null && statut == STATUT_APPROUVE;
    }

    public boolean estDossierIncomplet() {
        return statut != null && statut == STATUT_INCOMPLET;
    }
}
