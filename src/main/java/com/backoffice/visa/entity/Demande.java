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
    private Integer statut = STATUT_CREATION;

    public static final int STATUT_CREATION = 1;
    public static final int STATUT_PHOTO_PRISE = 2;
    public static final int STATUT_SCANNE = 3;
    public static final int STATUT_TERMINE = 4;

    public static final int STATUT_DUPLICATA_DEMANDE = 10;
    public static final int STATUT_DUPLICATA_SIGNATURE_TERMINEE = 11;
    public static final int STATUT_DUPLICATA_SCANNE = 12;
    public static final int STATUT_DUPLICATA_VALIDE = 13;
    public static final int STATUT_DUPLICATA_REJETE = 14;
    public static final int STATUT_DUPLICATA_EMIS = 15;

    public static final int STATUT_TRANSFERT_DEMANDE = 20;
    public static final int STATUT_TRANSFERT_SIGNATURE_TERMINEE = 21;
    public static final int STATUT_TRANSFERT_SCANNE = 22;
    public static final int STATUT_TRANSFERT_VALIDE = 23;
    public static final int STATUT_TRANSFERT_REJETE = 24;
    public static final int STATUT_TRANSFERT_EMIS = 25;

    @ManyToOne
    @JoinColumn(name = "id_statut", nullable = false, insertable = false, updatable = false)
    private Statut statutReference;

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
    public Statut getStatutReference() { return statutReference; }
    public void setStatutReference(Statut statutReference) { this.statutReference = statutReference; }
    public Demandeur getDemandeur() { return demandeur; }
    public void setDemandeur(Demandeur demandeur) { this.demandeur = demandeur; }
    public TypeVisa getTypeVisa() { return typeVisa; }
    public void setTypeVisa(TypeVisa typeVisa) { this.typeVisa = typeVisa; }
    public TypeDemande getTypeDemande() { return typeDemande; }
    public void setTypeDemande(TypeDemande typeDemande) { this.typeDemande = typeDemande; }
    public LocalDate getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDate dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getStatutLibelle() {
        if (statutReference != null) {
            return statutReference.getLibelle();
        }
        return switch (statut) {
            case STATUT_CREATION -> "Dossier cree";
            case STATUT_PHOTO_PRISE -> "Photo prise";
            case STATUT_SCANNE -> "Dossier scanne";
            case STATUT_TERMINE -> "Dossier termine";
            case STATUT_DUPLICATA_DEMANDE -> "Duplicata demande";
            case STATUT_DUPLICATA_SIGNATURE_TERMINEE -> "Duplicata signature terminee";
            case STATUT_DUPLICATA_SCANNE -> "Duplicata scanne";
            case STATUT_DUPLICATA_VALIDE -> "Duplicata valide";
            case STATUT_DUPLICATA_REJETE -> "Duplicata rejete";
            case STATUT_DUPLICATA_EMIS -> "Duplicata emis";
            case STATUT_TRANSFERT_DEMANDE -> "Transfert demande";
            case STATUT_TRANSFERT_SIGNATURE_TERMINEE -> "Transfert signature terminee";
            case STATUT_TRANSFERT_SCANNE -> "Transfert scanne";
            case STATUT_TRANSFERT_VALIDE -> "Transfert valide";
            case STATUT_TRANSFERT_REJETE -> "Transfert rejete";
            case STATUT_TRANSFERT_EMIS -> "Transfert emis";
            default -> "Inconnu (" + statut + ")";
        };
    }
}
